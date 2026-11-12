package io.github.blazevortex.core;

import io.github.blazevortex.api.BlazeVortex;
import io.github.blazevortex.api.audio.SoundRegistry;
import io.github.blazevortex.api.cosmetic.CosmeticRegistry;
import io.github.blazevortex.api.display.DisplayRegistry;
import io.github.blazevortex.api.effect.EffectRegistry;
import io.github.blazevortex.api.emoji.EmojiRegistry;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.gif.GifRegistry;
import io.github.blazevortex.api.hologram.HologramRegistry;
import io.github.blazevortex.api.image.ImageRegistry;
import io.github.blazevortex.api.item.CustomItemRegistry;
import io.github.blazevortex.api.module.ModuleManager;
import io.github.blazevortex.api.pack.ResourcePackService;
import io.github.blazevortex.api.video.VideoRegistry;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.config.ConfigLoader;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.content.ContentWatcher;
import io.github.blazevortex.cosmetic.CosmeticRegistryImpl;
import io.github.blazevortex.display.DisplayRegistryImpl;
import io.github.blazevortex.effect.EffectRegistryImpl;
import io.github.blazevortex.emoji.EmojiRegistryImpl;
import io.github.blazevortex.emoji.EmojiRendererImpl;
import io.github.blazevortex.font.GlyphAllocator;
import io.github.blazevortex.hologram.HologramRegistryImpl;
import io.github.blazevortex.item.CustomItemRegistryImpl;
import io.github.blazevortex.media.audio.SoundRegistryImpl;
import io.github.blazevortex.media.ffmpeg.FfmpegService;
import io.github.blazevortex.media.gif.GifDecoder;
import io.github.blazevortex.media.gif.GifRegistryImpl;
import io.github.blazevortex.media.image.ImageProcessor;
import io.github.blazevortex.media.image.ImageRegistryImpl;
import io.github.blazevortex.media.video.VideoRegistryImpl;
import io.github.blazevortex.pack.ResourcePackBuilder;
import io.github.blazevortex.pack.ResourcePackServiceImpl;
import io.github.blazevortex.platform.PlatformAdapter;
import io.github.blazevortex.security.UploadValidator;
import io.github.blazevortex.web.DashboardServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BlazeVortexEngine implements BlazeVortex {

    public static final String VERSION = "1.0.0";

    private final PlatformAdapter platform;
    private final Logger logger;
    private final ContentLayout layout;
    private final ConfigLoader configLoader;
    private final ServiceRegistry services = new ServiceRegistry();
    private final ModuleManagerImpl modules;
    private final BvxEventBus events = new BvxEventBus();

    private BvxConfig config;
    private ImageProcessor imageProcessor;
    private GifDecoder gifDecoder;
    private GlyphAllocator glyphs;
    private EmojiRegistryImpl emojis;
    private EmojiRendererImpl renderer;
    private ImageRegistryImpl images;
    private GifRegistryImpl gifs;
    private VideoRegistryImpl videos;
    private SoundRegistryImpl sounds;
    private DisplayRegistryImpl displays;
    private HologramRegistryImpl holograms;
    private CustomItemRegistryImpl items;
    private CosmeticRegistryImpl cosmetics;
    private EffectRegistryImpl effects;
    private ResourcePackBuilder packBuilder;
    private ResourcePackServiceImpl packs;
    private FfmpegService ffmpeg;
    private UploadValidator uploads;
    private ContentWatcher watcher;
    private DashboardServer dashboard;

    public BlazeVortexEngine(PlatformAdapter platform) {
        this.platform = platform;
        this.logger = platform.logger();
        this.layout = new ContentLayout(platform.dataFolder());
        this.configLoader = new ConfigLoader(logger);
        this.modules = new ModuleManagerImpl(logger);
    }

    public void start() throws IOException {
        layout.createDirectories();
        config = configLoader.load(layout.dataFolder().resolve("config.yml"), "config/config.yml");
        imageProcessor = new ImageProcessor(config.mediaLimits(), config.imageCacheSize());
        gifDecoder = new GifDecoder(config.mediaLimits());
        glyphs = new GlyphAllocator(layout.glyphRegistry());
        ffmpeg = new FfmpegService(config.ffmpegPath(), config.mediaLimits(), logger);
        uploads = new UploadValidator(config.mediaLimits());

        emojis = new EmojiRegistryImpl(layout, config, imageProcessor, gifDecoder, glyphs, events, logger, configLoader);
        renderer = new EmojiRendererImpl(emojis, true);
        images = new ImageRegistryImpl(layout, imageProcessor, platform.scheduler(), logger);
        gifs = new GifRegistryImpl(layout, gifDecoder, logger, config.gifCacheSize());
        videos = new VideoRegistryImpl(layout, config, ffmpeg, platform.scheduler(), events, logger, platform);
        sounds = new SoundRegistryImpl(layout, config, ffmpeg, platform.scheduler(), events, logger, platform);
        displays = new DisplayRegistryImpl(platform, events, layout, logger);
        holograms = new HologramRegistryImpl(platform, layout, configLoader, logger);
        items = new CustomItemRegistryImpl(layout, configLoader, logger);
        cosmetics = new CosmeticRegistryImpl(layout, configLoader, logger);
        effects = new EffectRegistryImpl(layout, configLoader, logger, platform);
        packBuilder = new ResourcePackBuilder(layout, config, emojis, imageProcessor, gifDecoder, logger);
        packs = new ResourcePackServiceImpl(
                packBuilder, platform.scheduler(), events, platform,
                () -> List.copyOf(sounds.assets()),
                config.packAutoSend(), config.packRequired()
        );

        services.register(BvxConfig.class, config);
        services.register(EmojiRegistryImpl.class, emojis);
        services.register(EmojiRendererImpl.class, renderer);
        services.register(ImageProcessor.class, imageProcessor);
        services.register(FfmpegService.class, ffmpeg);
        services.register(UploadValidator.class, uploads);
        services.register(BvxEventBus.class, events);
        services.register(PlatformAdapter.class, platform);

        registerModules();
        ModuleContextImpl context = new ModuleContextImpl(this, layout.dataFolder(), layout.content(), logger, services, config::moduleEnabled);
        modules.loadAll(context, config::moduleEnabled);

        if (config.moduleEnabled("emoji")) {
            emojis.reload();
        }
        if (config.moduleEnabled("image")) {
            images.discover();
        }
        if (config.moduleEnabled("gif")) {
            gifs.discover();
        }
        if (config.moduleEnabled("video")) {
            videos.discover();
        }
        if (config.moduleEnabled("audio")) {
            sounds.discover();
        }
        if (config.moduleEnabled("hologram")) {
            holograms.reload();
        }
        if (config.moduleEnabled("items")) {
            items.reload();
        }

        modules.enableAll(config::moduleEnabled);
        platform.applyChatFormatting(this);

        if (config.watchFiles()) {
            watcher = new ContentWatcher(layout.content(), logger, platform.scheduler(), path -> {
                logger.info("Content change detected: " + layout.content().relativize(path));
                platform.scheduler().runAsync(this::rediscover);
            });
            watcher.start();
        }
        if (config.webEnabled() && config.moduleEnabled("web")) {
            dashboard = new DashboardServer(this, config, layout, uploads, logger);
            dashboard.start();
        }
        logger.info("Blaze Vortex " + VERSION + " enabled on " + platform.serverBrand()
                + " / Minecraft " + platform.minecraftVersion());
    }

    public void shutdown() {
        if (watcher != null) {
            watcher.close();
        }
        if (dashboard != null) {
            dashboard.stop();
        }
        modules.disableAll();
        if (videos != null) {
            videos.shutdown();
        }
        if (sounds != null) {
            sounds.shutdown();
        }
        if (displays != null) {
            displays.shutdown();
        }
        platform.shutdownWorldObjects();
        platform.scheduler().cancelAll();
        logger.info("Blaze Vortex shut down");
    }

    private void registerModules() {
        modules.register(new ToggleModule("core", "Core Engine"));
        modules.register(new ToggleModule("emoji", "Emoji Engine"));
        modules.register(new ToggleModule("image", "Image Engine"));
        modules.register(new ToggleModule("gif", "GIF Animation Engine"));
        modules.register(new ToggleModule("video", "Video Engine"));
        modules.register(new ToggleModule("audio", "Audio Engine"));
        modules.register(new ToggleModule("resourcepack", "Resource Pack Engine"));
        modules.register(new ToggleModule("font", "Custom Font Engine"));
        modules.register(new ToggleModule("display", "Display Engine"));
        modules.register(new ToggleModule("hologram", "Hologram Engine"));
        modules.register(new ToggleModule("gui", "GUI Engine"));
        modules.register(new ToggleModule("items", "Custom Items Engine"));
        modules.register(new ToggleModule("cosmetics", "Cosmetics Engine"));
        modules.register(new ToggleModule("effects", "Effects Engine"));
        modules.register(new ToggleModule("config", "Configuration Engine"));
        modules.register(new ToggleModule("storage", "Storage and Caching Engine"));
        modules.register(new ToggleModule("api", "Developer API"));
        modules.register(new ToggleModule("integrations", "Integration Engine"));
        modules.register(new ToggleModule("web", "Web Dashboard"));
    }

    private void rediscover() {
        try {
            emojis.discoverUnconfigured();
            images.discover();
            gifs.discover();
            videos.discover();
            sounds.discover();
            if (config.autoRebuildPack()) {
                packs.build();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Hot reload of content failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public Path dataFolder() {
        return layout.dataFolder();
    }

    @Override
    public Path contentFolder() {
        return layout.content();
    }

    @Override
    public ModuleManager modules() {
        return modules;
    }

    @Override
    public EmojiRegistry emojis() {
        return emojis;
    }

    @Override
    public io.github.blazevortex.api.emoji.EmojiRenderer renderer() {
        return renderer;
    }

    @Override
    public ImageRegistry images() {
        return images;
    }

    @Override
    public GifRegistry gifs() {
        return gifs;
    }

    @Override
    public VideoRegistry videos() {
        return videos;
    }

    @Override
    public SoundRegistry sounds() {
        return sounds;
    }

    @Override
    public DisplayRegistry displays() {
        return displays;
    }

    @Override
    public HologramRegistry holograms() {
        return holograms;
    }

    @Override
    public CustomItemRegistry items() {
        return items;
    }

    @Override
    public CosmeticRegistry cosmetics() {
        return cosmetics;
    }

    @Override
    public EffectRegistry effects() {
        return effects;
    }

    @Override
    public ResourcePackService resourcePacks() {
        return packs;
    }

    @Override
    public void reload() {
        config = configLoader.load(layout.dataFolder().resolve("config.yml"), "config/config.yml");
        emojis.reload();
        images.discover();
        gifs.discover();
        holograms.reload();
        items.reload();
        cosmetics.reload();
        effects.reload();
        logger.info("Blaze Vortex configuration reloaded");
    }

    public BvxConfig config() {
        return config;
    }

    public BvxEventBus events() {
        return events;
    }

    public PlatformAdapter platform() {
        return platform;
    }

    public FfmpegService ffmpeg() {
        return ffmpeg;
    }

    public UploadValidator uploads() {
        return uploads;
    }

    public ContentLayout layout() {
        return layout;
    }

    public GlyphAllocator glyphs() {
        return glyphs;
    }

    public ResourcePackBuilder packBuilder() {
        return packBuilder;
    }

    private static final class ToggleModule extends AbstractModule {
        private ToggleModule(String id, String displayName) {
            super(id, displayName);
        }
    }
}
