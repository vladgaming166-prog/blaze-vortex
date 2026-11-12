package io.github.blazevortex.emoji;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.api.emoji.Emoji;
import io.github.blazevortex.api.emoji.EmojiRegistration;
import io.github.blazevortex.api.emoji.EmojiRegistry;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.event.EmojiRegisterEvent;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.config.ConfigLoader;
import io.github.blazevortex.content.AssetDiscovery;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.font.GlyphAllocator;
import io.github.blazevortex.media.gif.GifDecoder;
import io.github.blazevortex.media.gif.GifInfo;
import io.github.blazevortex.media.image.ImageMetadata;
import io.github.blazevortex.media.image.ImageProcessor;
import io.github.blazevortex.security.MagicBytes;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.security.PathSafety;
import io.github.blazevortex.util.Names;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EmojiRegistryImpl implements EmojiRegistry {

    private final ContentLayout layout;
    private final BvxConfig config;
    private final ImageProcessor images;
    private final GifDecoder gifs;
    private final GlyphAllocator glyphs;
    private final EmojiSizeEngine sizeEngine;
    private final BvxEventBus events;
    private final Logger logger;
    private final ConfigLoader loader;
    private final ConcurrentHashMap<String, RegisteredEmoji> emojis = new ConcurrentHashMap<>();

    public EmojiRegistryImpl(
            ContentLayout layout,
            BvxConfig config,
            ImageProcessor images,
            GifDecoder gifs,
            GlyphAllocator glyphs,
            BvxEventBus events,
            Logger logger,
            ConfigLoader loader
    ) {
        this.layout = layout;
        this.config = config;
        this.images = images;
        this.gifs = gifs;
        this.glyphs = glyphs;
        this.events = events;
        this.logger = logger;
        this.loader = loader;
        this.sizeEngine = new EmojiSizeEngine(
                config.emojiReferenceHeight(),
                config.emojiNativePixelReference(),
                config.emojiMaxGlyphHeight(),
                config.emojiMaxGlyphWidth()
        );
    }

    public EmojiSizeEngine sizeEngine() {
        return sizeEngine;
    }

    public GlyphAllocator glyphs() {
        return glyphs;
    }

    @Override
    public Collection<Emoji> all() {
        return List.copyOf(emojis.values());
    }

    public Collection<RegisteredEmoji> registered() {
        return List.copyOf(emojis.values());
    }

    @Override
    public Optional<Emoji> find(String name) {
        return Optional.ofNullable(emojis.get(name.toLowerCase()));
    }

    public Optional<RegisteredEmoji> findRegistered(String name) {
        return Optional.ofNullable(emojis.get(name.toLowerCase()));
    }

    @Override
    public Emoji register(EmojiRegistration registration) {
        try {
            RegisteredEmoji emoji = create(registration);
            EmojiRegisterEvent event = events.fire(new EmojiRegisterEvent(emoji));
            if (event.isCancelled()) {
                throw new BvxException("Emoji registration cancelled for " + registration.name());
            }
            emojis.put(emoji.name(), emoji);
            return emoji;
        } catch (IOException ex) {
            throw new BvxException("Failed to register emoji " + registration.name() + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean unregister(String name) {
        return emojis.remove(name.toLowerCase()) != null;
    }

    @Override
    public void reload() {
        emojis.clear();
        loadFromYaml();
        discoverUnconfigured();
    }

    @Override
    public Path contentDirectory() {
        return layout.emojis();
    }

    public void loadFromYaml() {
        Path file = layout.dataFolder().resolve("emojis.yml");
        Map<String, Object> raw = loader.loadRaw(file, "config/emojis.yml");
        Object emojisNode = raw.get("emojis");
        if (!(emojisNode instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String name = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                logger.warning("Ignoring invalid emoji entry: " + name);
                continue;
            }
            try {
                register(fromYaml(name, values));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to load emoji '" + name + "': " + ex.getMessage(), ex);
            }
        }
    }

    public void discoverUnconfigured() {
        try {
            List<Path> files = AssetDiscovery.discover(layout.content(), AssetDiscovery.kinds(
                    MediaKind.PNG, MediaKind.JPEG, MediaKind.GIF, MediaKind.WEBP));
            for (Path file : files) {
                String name = Names.assetName(file);
                if (emojis.containsKey(name)) {
                    continue;
                }
                try {
                    MagicBytes.detect(file).ifPresent(kind -> {
                        if (!kind.isImage()) {
                            return;
                        }
                        register(EmojiRegistration.builder(name, file)
                                .animated(kind == MediaKind.GIF)
                                .size(100)
                                .build());
                    });
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Auto-registration failed for " + file.getFileName() + ": " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Emoji discovery failed: " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private EmojiRegistration fromYaml(String name, Map<?, ?> values) {
        String fileValue = io.github.blazevortex.util.Maps.getString(values, "file", "emojis/" + name + ".png");
        Path file = PathSafety.resolveInside(layout.content(), fileValue);
        if (!Files.isRegularFile(file)) {
            throw new BvxException("Emoji file missing: " + fileValue);
        }
        Object sizeValue = io.github.blazevortex.util.Maps.get(values, "size", 100);
        int size = sizeValue instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(sizeValue));
        EmojiSizeEngine.validateSize(size);
        EmojiRegistration.Builder builder = EmojiRegistration.builder(Names.requireSafe(name), file)
                .enabled(asBoolean(values.get("enabled"), true))
                .size(size)
                .yOffset(asInt(values.get("y-offset"), 0))
                .animated(asBoolean(values.get("animated"), fileValue.toLowerCase().endsWith(".gif")))
                .loop(asBoolean(values.get("loop"), true))
                .playbackSpeed(asDouble(values.get("playback-speed"), 1.0));
        if (values.get("fps") != null) {
            builder.fps(asInt(values.get("fps"), 10));
        }
        Object context = values.get("context-sizes");
        if (context instanceof Map<?, ?> ctx) {
            ctx.forEach((k, v) -> {
                try {
                    builder.contextSize(RenderContext.valueOf(String.valueOf(k).toUpperCase().replace('-', '_')),
                            asInt(v, 100));
                } catch (IllegalArgumentException ignored) {
                    logger.warning("Unknown emoji context '" + k + "' for " + name);
                }
            });
        }
        config.emojiContextSizes().forEach((k, v) -> {
            try {
                builder.contextSize(RenderContext.valueOf(k.toUpperCase().replace('-', '_')), v);
            } catch (IllegalArgumentException ignored) {
            }
        });
        return builder.build();
    }

    private RegisteredEmoji create(EmojiRegistration registration) throws IOException {
        Path file = registration.file();
        MediaKind kind = MagicBytes.detect(file).orElseThrow(() ->
                new BvxException("Unsupported emoji file: " + file.getFileName()));
        if (!kind.isImage()) {
            throw new BvxException("Emoji must be an image, got " + kind);
        }
        int width;
        int height;
        char[] frames;
        boolean animated = registration.animated() || kind == MediaKind.GIF;
        if (kind == MediaKind.GIF && animated) {
            GifInfo info = gifs.probe(file);
            width = info.width();
            height = info.height();
            frames = new char[Math.max(1, info.frameCount())];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = glyphs.allocate("emoji:" + registration.name() + ":frame:" + i);
            }
        } else {
            ImageMetadata meta = images.probe(file);
            width = meta.width();
            height = meta.height();
            frames = new char[]{glyphs.allocate("emoji:" + registration.name())};
        }
        GlyphMetrics metrics = sizeEngine.calculate(width, height, registration.size(), registration.yOffset());
        return new RegisteredEmoji(registration, width, height, metrics, frames[0], frames);
    }

    private static boolean asBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return fallback;
    }

    private static int asInt(Object value, int fallback) {
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

    private static double asDouble(Object value, double fallback) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }

    public String dumpYamlPreview() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> map = new LinkedHashMap<>();
        for (RegisteredEmoji emoji : emojis.values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("file", layout.content().relativize(emoji.file()).toString().replace('\\', '/'));
            entry.put("enabled", emoji.enabled());
            entry.put("size", emoji.size());
            entry.put("y-offset", emoji.yOffset());
            entry.put("animated", emoji.animated());
            map.put(emoji.name(), entry);
        }
        root.put("emojis", map);
        return new Yaml().dump(root);
    }
}
