package io.github.blazevortex.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.audio.SoundAsset;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.api.pack.ResourcePackStatus;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.emoji.EmojiRegistryImpl;
import io.github.blazevortex.emoji.RegisteredEmoji;
import io.github.blazevortex.media.gif.GifDecoder;
import io.github.blazevortex.media.gif.GifFrame;
import io.github.blazevortex.media.gif.GifSequence;
import io.github.blazevortex.media.image.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates a Minecraft resource pack from registered emojis, GIF frames, images and sounds.
 *
 * <p>Font bitmaps do not animate through {@code .png.mcmeta}. Animated emoji glyphs are
 * exported as one bitmap per GIF frame so updatable UIs can cycle characters. World displays
 * additionally receive animated textures where Minecraft supports them.
 */
public final class ResourcePackBuilder {

    public static final String NAMESPACE = "blazevortex";

    private final ContentLayout layout;
    private final BvxConfig config;
    private final EmojiRegistryImpl emojis;
    private final ImageProcessor images;
    private final GifDecoder gifs;
    private final Logger logger;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final AtomicReference<ResourcePackStatus> status = new AtomicReference<>(ResourcePackStatus.NOT_BUILT);
    private volatile ResourcePackBuildResult lastResult;

    public ResourcePackBuilder(
            ContentLayout layout,
            BvxConfig config,
            EmojiRegistryImpl emojis,
            ImageProcessor images,
            GifDecoder gifs,
            Logger logger
    ) {
        this.layout = layout;
        this.config = config;
        this.emojis = emojis;
        this.images = images;
        this.gifs = gifs;
        this.logger = logger;
    }

    public ResourcePackStatus status() {
        return status.get();
    }

    public ResourcePackBuildResult lastResult() {
        return lastResult;
    }

    public ResourcePackBuildResult build(List<SoundAsset> sounds, String minecraftVersion) {
        status.set(ResourcePackStatus.BUILDING);
        List<String> warnings = new ArrayList<>();
        Path generated = layout.generatedPack();
        try {
            backupExisting(layout.packExports());
            wipeGenerated(generated);
            Path assets = generated.resolve("assets").resolve(NAMESPACE);
            Path fontDir = assets.resolve("font");
            Path emojiTex = assets.resolve("textures").resolve("font").resolve("emoji");
            Path soundDir = assets.resolve("sounds");
            Files.createDirectories(fontDir);
            Files.createDirectories(emojiTex);
            Files.createDirectories(soundDir);

            JsonArray providers = new JsonArray();
            providers.add(spaceProvider());
            int glyphCount = 0;
            for (RegisteredEmoji emoji : emojis.registered()) {
                if (!emoji.enabled()) {
                    continue;
                }
                try {
                    glyphCount += writeEmoji(emoji, emojiTex, providers, warnings);
                } catch (Exception ex) {
                    warnings.add("Emoji '" + emoji.name() + "' could not be packed: " + ex.getMessage());
                    logger.log(Level.WARNING, "Failed to pack emoji " + emoji.name(), ex);
                }
            }
            writeFont(fontDir, "emoji.json", providers);
            writeFont(fontDir, "emoji_scoreboard.json", providers);
            writeFont(fontDir, "emoji_tab.json", providers);
            writeFont(fontDir, "emoji_gui.json", providers);

            JsonObject soundsJson = new JsonObject();
            int soundCount = 0;
            for (SoundAsset sound : sounds) {
                if (sound.oggFile().isEmpty() || !Files.isRegularFile(sound.oggFile().get())) {
                    warnings.add("Sound '" + sound.name() + "' has no OGG conversion yet and was skipped");
                    continue;
                }
                Path target = soundDir.resolve(sound.name() + ".ogg");
                Files.copy(sound.oggFile().get(), target, StandardCopyOption.REPLACE_EXISTING);
                JsonObject event = new JsonObject();
                JsonArray entries = new JsonArray();
                JsonObject entry = new JsonObject();
                entry.addProperty("name", NAMESPACE + ":" + sound.name());
                entry.addProperty("stream", true);
                entries.add(entry);
                event.add("sounds", entries);
                soundsJson.add(NAMESPACE + "." + sound.name(), event);
                soundCount++;
            }
            Files.writeString(assets.resolve("sounds.json"), gson.toJson(soundsJson), StandardCharsets.UTF_8);

            int packFormat = config.packFormatOverride() > 0
                    ? config.packFormatOverride()
                    : PackFormatCatalog.forMinecraftVersion(minecraftVersion);
            Files.writeString(generated.resolve("pack.mcmeta"), gson.toJson(packMcmeta(packFormat)), StandardCharsets.UTF_8);
            Files.writeString(generated.resolve("pack.json"), gson.toJson(Map.of(
                    "generatedAt", Instant.now().toString(),
                    "glyphs", glyphCount,
                    "sounds", soundCount,
                    "packFormat", packFormat
            )), StandardCharsets.UTF_8);
            validateJson(generated);

            Path zip = layout.packExports().resolve("BlazeVortex-pack.zip");
            zipDirectory(generated, zip);
            String sha1 = sha1(zip);
            Files.writeString(layout.packExports().resolve("sha1.txt"), sha1, StandardCharsets.UTF_8);
            ResourcePackBuildResult result = new ResourcePackBuildResult(
                    true, zip, sha1, Files.size(zip), glyphCount, soundCount, List.copyOf(warnings), null);
            lastResult = result;
            status.set(ResourcePackStatus.READY);
            return result;
        } catch (Exception ex) {
            status.set(ResourcePackStatus.FAILED);
            ResourcePackBuildResult failed = ResourcePackBuildResult.failed(ex.getMessage());
            lastResult = failed;
            logger.log(Level.SEVERE, "Resource pack build failed", ex);
            return failed;
        }
    }

    private int writeEmoji(RegisteredEmoji emoji, Path emojiTex, JsonArray providers, List<String> warnings)
            throws IOException {
        Path file = emoji.file();
        if (emoji.animated()) {
            GifSequence sequence = gifs.decode(file);
            char[] glyphs = emoji.animationGlyphs();
            int frames = Math.min(sequence.frameCount(), glyphs.length);
            int written = 0;
            List<Path> framePngs = new ArrayList<>();
            for (int i = 0; i < frames; i++) {
                GifFrame frame = sequence.frames().get(i);
                Path png = emojiTex.resolve(emoji.name() + "_" + i + ".png");
                ImageIO.write(ensureArgb(frame.image()), "png", png.toFile());
                providers.add(bitmapProvider("blazevortex:font/emoji/" + emoji.name() + "_" + i, glyphs[i], emoji));
                framePngs.add(png);
                written++;
            }
            writeMcmetaAnimation(emojiTex.resolve(emoji.name() + ".png"), sequence);
            if (!framePngs.isEmpty()) {
                Files.copy(framePngs.get(0), emojiTex.resolve(emoji.name() + ".png"), StandardCopyOption.REPLACE_EXISTING);
            }
            return written;
        }
        BufferedImage image = images.read(file);
        Path png = emojiTex.resolve(emoji.name() + ".png");
        ImageIO.write(ensureArgb(image), "png", png.toFile());
        providers.add(bitmapProvider("blazevortex:font/emoji/" + emoji.name(), emoji.glyph(), emoji));
        if (image.getWidth() != emoji.sourceWidth() || image.getHeight() != emoji.sourceHeight()) {
            warnings.add("Emoji " + emoji.name() + " PNG dimensions changed during decode");
        }
        return 1;
    }

    private JsonObject bitmapProvider(String texture, char glyph, RegisteredEmoji emoji) {
        JsonObject provider = new JsonObject();
        provider.addProperty("type", "bitmap");
        provider.addProperty("file", texture + ".png");
        provider.addProperty("ascent", emoji.ascent());
        provider.addProperty("height", emoji.glyphHeight());
        JsonArray chars = new JsonArray();
        chars.add(String.valueOf(glyph));
        provider.add("chars", chars);
        return provider;
    }

    private JsonObject spaceProvider() {
        JsonObject provider = new JsonObject();
        provider.addProperty("type", "space");
        JsonObject advances = new JsonObject();
        advances.addProperty(" ", 4);
        provider.add("advances", advances);
        return provider;
    }

    private void writeFont(Path fontDir, String name, JsonArray providers) throws IOException {
        JsonObject root = new JsonObject();
        root.add("providers", providers.deepCopy());
        Files.writeString(fontDir.resolve(name), gson.toJson(root), StandardCharsets.UTF_8);
    }

    private JsonObject packMcmeta(int packFormat) {
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", packFormat);
        pack.addProperty("description", "Blaze Vortex generated resource pack");
        JsonObject supported = new JsonObject();
        supported.addProperty("min_inclusive", PackFormatCatalog.minFormat());
        supported.addProperty("max_inclusive", PackFormatCatalog.maxFormat());
        pack.add("supported_formats", supported);
        JsonArray minFormat = new JsonArray();
        minFormat.add(PackFormatCatalog.minFormat());
        minFormat.add(0);
        JsonArray maxFormat = new JsonArray();
        maxFormat.add(PackFormatCatalog.maxFormat());
        maxFormat.add(0);
        pack.add("min_format", minFormat);
        pack.add("max_format", maxFormat);
        JsonObject root = new JsonObject();
        root.add("pack", pack);
        return root;
    }

    private void writeMcmetaAnimation(Path png, GifSequence sequence) throws IOException {
        JsonObject animation = new JsonObject();
        animation.addProperty("interpolate", false);
        int avg = (int) Math.max(1, Math.round(sequence.frames().stream().mapToInt(GifFrame::delayMs).average().orElse(100) / 50.0));
        animation.addProperty("frametime", avg);
        JsonArray frames = new JsonArray();
        for (int i = 0; i < sequence.frameCount(); i++) {
            frames.add(i);
        }
        animation.add("frames", frames);
        JsonObject root = new JsonObject();
        root.add("animation", animation);
        Files.writeString(Path.of(png.toString() + ".mcmeta"), gson.toJson(root), StandardCharsets.UTF_8);
    }

    private BufferedImage ensureArgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }
        BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = converted.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return converted;
    }

    private void wipeGenerated(Path generated) throws IOException {
        if (Files.exists(generated)) {
            try (var stream = Files.walk(generated)) {
                stream.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(path -> {
                    try {
                        if (!path.equals(generated)) {
                            Files.deleteIfExists(path);
                        }
                    } catch (IOException ignored) {
                    }
                });
            }
        }
        Files.createDirectories(generated);
    }

    private void backupExisting(Path exports) throws IOException {
        Files.createDirectories(exports);
        Path current = exports.resolve("BlazeVortex-pack.zip");
        if (Files.exists(current)) {
            Path backup = exports.resolve("BlazeVortex-pack-" + System.currentTimeMillis() + ".zip.bak");
            Files.move(current, backup, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validateJson(Path generated) throws IOException {
        try (var stream = Files.walk(generated)) {
            List<Path> jsonFiles = stream.filter(path -> path.toString().endsWith(".json") || path.toString().endsWith(".mcmeta"))
                    .toList();
            for (Path json : jsonFiles) {
                String text = Files.readString(json, StandardCharsets.UTF_8);
                try {
                    JsonParser.parseString(text);
                } catch (Exception ex) {
                    throw new BvxException("Invalid JSON generated at " + generated.relativize(json) + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    private void zipDirectory(Path source, Path zip) throws IOException {
        Files.createDirectories(zip.getParent());
        try (OutputStream out = Files.newOutputStream(zip);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            try (var stream = Files.walk(source)) {
                for (Path path : stream.filter(Files::isRegularFile).toList()) {
                    String entryName = source.relativize(path).toString().replace('\\', '/');
                    zipOut.putNextEntry(new ZipEntry(entryName));
                    try (InputStream in = Files.newInputStream(path)) {
                        in.transferTo(zipOut);
                    }
                    zipOut.closeEntry();
                }
            }
        }
    }

    private String sha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
