package io.github.blazevortex.config;

import io.github.blazevortex.security.MediaLimits;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class BvxConfig {

    private final Map<String, Boolean> modules;
    private final MediaLimits mediaLimits;
    private final int emojiReferenceHeight;
    private final int emojiNativePixelReference;
    private final int emojiMaxGlyphHeight;
    private final int emojiMaxGlyphWidth;
    private final int imageCacheSize;
    private final int gifCacheSize;
    private final int videoFrameCacheSize;
    private final boolean watchFiles;
    private final boolean autoRebuildPack;
    private final String packUrl;
    private final boolean packRequired;
    private final boolean packAutoSend;
    private final int packFormatOverride;
    private final String ffmpegPath;
    private final int defaultVideoFps;
    private final int defaultVideoWidth;
    private final int defaultVideoHeight;
    private final int maxConcurrentVideos;
    private final boolean webEnabled;
    private final String webBind;
    private final int webPort;
    private final boolean webRemoteAuth;
    private final String webPasswordHash;
    private final String webPasswordSalt;
    private final int hologramUpdateTicks;
    private final int uiUpdateTicks;
    private final int soundTickRadiusCheck;
    private final Set<String> disabledWorlds;
    private final Map<String, Integer> emojiContextSizes;
    private final String language;
    private final boolean debug;
    private final String configVersion;

    @SuppressWarnings("unchecked")
    public BvxConfig(Map<String, Object> root) {
        Map<String, Object> moduleMap = map(root, "modules");
        Map<String, Boolean> parsedModules = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : moduleMap.entrySet()) {
            parsedModules.put(entry.getKey(), asBoolean(entry.getValue(), true));
        }
        this.modules = Collections.unmodifiableMap(parsedModules);

        Map<String, Object> security = map(root, "security");
        this.mediaLimits = new MediaLimits(
                asLong(security.get("max-upload-bytes"), 50L * 1024 * 1024),
                asInt(security.get("max-image-width"), 4096),
                asInt(security.get("max-image-height"), 4096),
                asLong(security.get("max-decoded-pixels"), 16L * 1024 * 1024),
                asInt(security.get("max-gif-frames"), 256),
                asInt(security.get("max-video-width"), 1920),
                asInt(security.get("max-video-height"), 1080),
                asInt(security.get("max-video-duration-seconds"), 180),
                asInt(security.get("max-audio-duration-seconds"), 300),
                asInt(security.get("max-fps"), 30),
                asInt(security.get("ffmpeg-timeout-seconds"), 60)
        );

        Map<String, Object> emoji = map(root, "emoji");
        this.emojiReferenceHeight = asInt(emoji.get("reference-height"), 16);
        this.emojiNativePixelReference = asInt(emoji.get("native-pixel-reference"), 128);
        this.emojiMaxGlyphHeight = asInt(emoji.get("max-glyph-height"), 256);
        this.emojiMaxGlyphWidth = asInt(emoji.get("max-glyph-width"), 256);
        Map<String, Object> context = map(emoji, "context-sizes");
        Map<String, Integer> ctx = new LinkedHashMap<>();
        context.forEach((k, v) -> ctx.put(k, asInt(v, 100)));
        this.emojiContextSizes = Collections.unmodifiableMap(ctx);

        Map<String, Object> cache = map(root, "cache");
        this.imageCacheSize = asInt(cache.get("images"), 64);
        this.gifCacheSize = asInt(cache.get("gifs"), 16);
        this.videoFrameCacheSize = asInt(cache.get("video-frames"), 24);

        Map<String, Object> content = map(root, "content");
        this.watchFiles = asBoolean(content.get("watch-files"), true);
        this.autoRebuildPack = asBoolean(content.get("auto-rebuild-pack"), false);

        Map<String, Object> pack = map(root, "resource-pack");
        this.packUrl = asString(pack.get("url"), "");
        this.packRequired = asBoolean(pack.get("required"), false);
        this.packAutoSend = asBoolean(pack.get("auto-send"), true);
        this.packFormatOverride = asInt(pack.get("pack-format"), 0);

        Map<String, Object> video = map(root, "video");
        this.ffmpegPath = asString(map(root, "ffmpeg").get("path"), "ffmpeg");
        this.defaultVideoFps = asInt(video.get("default-fps"), 10);
        this.defaultVideoWidth = asInt(video.get("default-width"), 128);
        this.defaultVideoHeight = asInt(video.get("default-height"), 128);
        this.maxConcurrentVideos = asInt(video.get("max-concurrent"), 4);

        Map<String, Object> web = map(root, "web");
        this.webEnabled = asBoolean(web.get("enabled"), false);
        this.webBind = asString(web.get("bind"), "127.0.0.1");
        this.webPort = asInt(web.get("port"), 8765);
        this.webRemoteAuth = asBoolean(web.get("require-auth-when-remote"), true);
        this.webPasswordHash = asString(web.get("password-hash"), "");
        this.webPasswordSalt = asString(web.get("password-salt"), "");

        Map<String, Object> performance = map(root, "performance");
        this.hologramUpdateTicks = asInt(performance.get("hologram-update-ticks"), 10);
        this.uiUpdateTicks = asInt(performance.get("ui-update-ticks"), 20);
        this.soundTickRadiusCheck = asInt(performance.get("sound-radius-ticks"), 20);

        this.disabledWorlds = toStringSet(root.get("disabled-worlds"));
        this.language = asString(root.get("language"), "en");
        this.debug = asBoolean(root.get("debug"), false);
        this.configVersion = asString(root.get("config-version"), "1");
    }

    public boolean moduleEnabled(String id) {
        return modules.getOrDefault(id, true);
    }

    public Map<String, Boolean> modules() {
        return modules;
    }

    public MediaLimits mediaLimits() {
        return mediaLimits;
    }

    public int emojiReferenceHeight() {
        return emojiReferenceHeight;
    }

    public int emojiNativePixelReference() {
        return emojiNativePixelReference;
    }

    public int emojiMaxGlyphHeight() {
        return emojiMaxGlyphHeight;
    }

    public int emojiMaxGlyphWidth() {
        return emojiMaxGlyphWidth;
    }

    public int imageCacheSize() {
        return imageCacheSize;
    }

    public int gifCacheSize() {
        return gifCacheSize;
    }

    public int videoFrameCacheSize() {
        return videoFrameCacheSize;
    }

    public boolean watchFiles() {
        return watchFiles;
    }

    public boolean autoRebuildPack() {
        return autoRebuildPack;
    }

    public String packUrl() {
        return packUrl;
    }

    public boolean packRequired() {
        return packRequired;
    }

    public boolean packAutoSend() {
        return packAutoSend;
    }

    public int packFormatOverride() {
        return packFormatOverride;
    }

    public String ffmpegPath() {
        return ffmpegPath;
    }

    public int defaultVideoFps() {
        return defaultVideoFps;
    }

    public int defaultVideoWidth() {
        return defaultVideoWidth;
    }

    public int defaultVideoHeight() {
        return defaultVideoHeight;
    }

    public int maxConcurrentVideos() {
        return maxConcurrentVideos;
    }

    public boolean webEnabled() {
        return webEnabled;
    }

    public String webBind() {
        return webBind;
    }

    public int webPort() {
        return webPort;
    }

    public boolean webRemoteAuth() {
        return webRemoteAuth;
    }

    public String webPasswordHash() {
        return webPasswordHash;
    }

    public String webPasswordSalt() {
        return webPasswordSalt;
    }

    public int hologramUpdateTicks() {
        return hologramUpdateTicks;
    }

    public int uiUpdateTicks() {
        return uiUpdateTicks;
    }

    public int soundTickRadiusCheck() {
        return soundTickRadiusCheck;
    }

    public Set<String> disabledWorlds() {
        return disabledWorlds;
    }

    public Map<String, Integer> emojiContextSizes() {
        return emojiContextSizes;
    }

    public String language() {
        return language;
    }

    public boolean debug() {
        return debug;
    }

    public String configVersion() {
        return configVersion;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    static boolean asBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return fallback;
    }

    static int asInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    static long asLong(Object value, long fallback) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    static String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    static Set<String> toStringSet(Object value) {
        if (value instanceof Iterable<?> iterable) {
            java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
            for (Object item : iterable) {
                set.add(String.valueOf(item));
            }
            return Set.copyOf(set);
        }
        return Set.of();
    }
}
