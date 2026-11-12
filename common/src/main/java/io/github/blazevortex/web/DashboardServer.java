package io.github.blazevortex.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.blazevortex.api.emoji.Emoji;
import io.github.blazevortex.api.emoji.EmojiRegistration;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.core.BlazeVortexEngine;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.security.PathSafety;
import io.github.blazevortex.security.UploadValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DashboardServer {

    private final BlazeVortexEngine engine;
    private final BvxConfig config;
    private final ContentLayout layout;
    private final UploadValidator uploads;
    private final Logger logger;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private HttpServer server;

    public DashboardServer(BlazeVortexEngine engine, BvxConfig config, ContentLayout layout, UploadValidator uploads, Logger logger) {
        this.engine = engine;
        this.config = config;
        this.layout = layout;
        this.uploads = uploads;
        this.logger = logger;
    }

    public void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress(config.webBind(), config.webPort());
        if (!address.getAddress().isLoopbackAddress() && config.webRemoteAuth()
                && (config.webPasswordHash() == null || config.webPasswordHash().isBlank())) {
            throw new IOException("Web dashboard refused to bind remotely without password-hash in config.yml");
        }
        server = HttpServer.create(address, 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "bvx-dashboard");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        logger.info("BVX dashboard listening on http://" + config.webBind() + ":" + config.webPort());
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.contains("..")) {
                send(exchange, 400, "text/plain", "Bad path");
                return;
            }
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Allow", "GET,POST,OPTIONS");
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            boolean loopback = exchange.getRemoteAddress().getAddress().isLoopbackAddress();
            boolean needsAuth = !loopback && config.webRemoteAuth();
            if (needsAuth && !path.equals("/api/login") && !authorized(exchange)) {
                send(exchange, 401, "application/json", "{\"error\":\"authentication required\"}");
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && "/".equals(path)) {
                send(exchange, 200, "text/html; charset=utf-8", DashboardPages.html());
                return;
            }
            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
                return;
            }
            if (path.startsWith("/preview/")) {
                handlePreview(exchange, path.substring("/preview/".length()));
                return;
            }
            send(exchange, 404, "text/plain", "Not found");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Dashboard error: " + ex.getMessage(), ex);
            send(exchange, 500, "application/json", "{\"error\":\"" + escape(ex.getMessage()) + "\"}");
        } finally {
            exchange.close();
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        switch (path) {
            case "/api/login" -> {
                Map<String, Object> body = readJson(exchange);
                String password = String.valueOf(body.getOrDefault("password", ""));
                if (!PasswordHasher.verify(password, config.webPasswordHash(), config.webPasswordSalt())) {
                    send(exchange, 403, "application/json", "{\"error\":\"invalid password\"}");
                    return;
                }
                String token = UUID.randomUUID().toString();
                sessions.put(token, System.currentTimeMillis());
                send(exchange, 200, "application/json", "{\"token\":\"" + token + "\"}");
            }
            case "/api/status" -> sendJson(exchange, status());
            case "/api/emojis" -> {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, Object> body = readJson(exchange);
                    String name = String.valueOf(body.get("name"));
                    int size = ((Number) body.get("size")).intValue();
                    Emoji existing = engine.emojis().find(name).orElseThrow();
                    engine.emojis().register(EmojiRegistration.builder(name, existing.file())
                            .size(size)
                            .yOffset(existing.yOffset())
                            .animated(existing.animated())
                            .enabled(existing.enabled())
                            .build());
                    sendJson(exchange, Map.of("ok", true, "size", size));
                } else {
                    sendJson(exchange, engine.emojis().all().stream().map(e -> Map.of(
                            "name", e.name(),
                            "size", e.size(),
                            "width", e.sourceWidth(),
                            "height", e.sourceHeight(),
                            "glyphHeight", e.glyphHeight(),
                            "glyphWidth", e.glyphWidth(),
                            "animated", e.animated()
                    )).toList());
                }
            }
            case "/api/modules" -> sendJson(exchange, engine.modules().modules().stream().map(m -> Map.of(
                    "id", m.id(),
                    "name", m.displayName(),
                    "enabled", m.isEnabled(),
                    "state", m.state().name()
            )).toList());
            case "/api/pack/build" -> {
                engine.resourcePacks().build();
                sendJson(exchange, Map.of("ok", true, "status", engine.resourcePacks().status().name()));
            }
            case "/api/upload" -> handleUpload(exchange);
            default -> send(exchange, 404, "application/json", "{\"error\":\"unknown endpoint\"}");
        }
    }

    private void handleUpload(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String category = queryValue(query, "category", "images");
        String filename = PathSafety.sanitizeFileName(queryValue(query, "filename", "upload.bin"));
        Path dir = switch (category) {
            case "emojis" -> layout.emojis();
            case "gifs" -> layout.gifs();
            case "videos" -> layout.videos();
            case "sounds" -> layout.sounds();
            default -> layout.images();
        };
        Path target = PathSafety.resolveInside(dir, filename);
        Files.createDirectories(target.getParent());
        Path temp = layout.temp().resolve("upload-" + UUID.randomUUID());
        Files.createDirectories(temp.getParent());
        try (InputStream in = exchange.getRequestBody()) {
            Files.copy(in, temp);
        }
        try {
            MediaKind kind = uploads.validate(temp, filename);
            if (category.equals("videos") && !kind.isVideo()) {
                throw new IOException("Not a video");
            }
            if (category.equals("sounds") && !kind.isAudio()) {
                throw new IOException("Not audio");
            }
            Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            engine.reload();
            sendJson(exchange, Map.of("ok", true, "file", filename, "kind", kind.name()));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private void handlePreview(HttpExchange exchange, String rest) throws IOException {
        String[] parts = rest.split("/", 2);
        if (parts.length != 2) {
            send(exchange, 400, "text/plain", "Bad preview path");
            return;
        }
        Path file = switch (parts[0]) {
            case "emoji", "emojis" -> PathSafety.resolveInside(layout.emojis(), parts[1]);
            case "image", "images" -> PathSafety.resolveInside(layout.images(), parts[1]);
            case "gif", "gifs" -> PathSafety.resolveInside(layout.gifs(), parts[1]);
            default -> throw new IOException("Preview not allowed for " + parts[0]);
        };
        if (!Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain", "Missing");
            return;
        }
        String mime = MediaKind.fromExtension(file.getFileName().toString()).map(MediaKind::mime).orElse("application/octet-stream");
        byte[] data = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Blaze Vortex");
        map.put("version", engine.version());
        map.put("emojis", engine.emojis().all().size());
        map.put("images", engine.images().all().size());
        map.put("gifs", engine.gifs().all().size());
        map.put("videos", engine.videos().assets().size());
        map.put("sounds", engine.sounds().assets().size());
        map.put("ffmpeg", engine.ffmpeg().available());
        map.put("pack", engine.resourcePacks().status().name());
        map.put("glyphs", engine.glyphs().size());
        return map;
    }

    private boolean authorized(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return false;
        }
        String token = header.substring("Bearer ".length());
        Long issued = sessions.get(token);
        return issued != null && System.currentTimeMillis() - issued < 12 * 60 * 60 * 1000L;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            return Map.of();
        }
        return gson.fromJson(body, Map.class);
    }

    private void sendJson(HttpExchange exchange, Object value) throws IOException {
        send(exchange, 200, "application/json", gson.toJson(value));
    }

    private void send(HttpExchange exchange, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String queryValue(String query, String key, String fallback) {
        if (query == null) {
            return fallback;
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return fallback;
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
