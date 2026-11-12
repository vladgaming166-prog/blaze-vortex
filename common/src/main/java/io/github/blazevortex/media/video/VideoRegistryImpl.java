package io.github.blazevortex.media.video;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.video.VideoAsset;
import io.github.blazevortex.api.video.VideoRegistry;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.api.video.VideoScreenCreateRequest;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.content.AssetDiscovery;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.media.ffmpeg.FfmpegService;
import io.github.blazevortex.security.MagicBytes;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.util.Names;
import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.PlatformAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VideoRegistryImpl implements VideoRegistry {

    private final ContentLayout layout;
    private final BvxConfig config;
    private final FfmpegService ffmpeg;
    private final BvxScheduler scheduler;
    private final BvxEventBus events;
    private final Logger logger;
    private final PlatformAdapter platform;
    private final ConcurrentHashMap<String, VideoAssetImpl> assets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VideoScreenImpl> screens = new ConcurrentHashMap<>();
    private final Semaphore processing;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public VideoRegistryImpl(
            ContentLayout layout,
            BvxConfig config,
            FfmpegService ffmpeg,
            BvxScheduler scheduler,
            BvxEventBus events,
            Logger logger,
            PlatformAdapter platform
    ) {
        this.layout = layout;
        this.config = config;
        this.ffmpeg = ffmpeg;
        this.scheduler = scheduler;
        this.events = events;
        this.logger = logger;
        this.platform = platform;
        this.processing = new Semaphore(Math.max(1, config.maxConcurrentVideos()));
    }

    public void discover() {
        try {
            for (Path file : AssetDiscovery.discover(layout.videos(), AssetDiscovery.kinds(MediaKind.MP4, MediaKind.MOV))) {
                try {
                    registerFile(file);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Video '" + file.getFileName() + "' rejected: " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Video discovery failed: " + ex.getMessage(), ex);
        }
        loadScreens();
    }

    public VideoAssetImpl registerFile(Path file) throws IOException {
        MediaKind kind = MagicBytes.detect(file).orElseThrow(() ->
                new BvxException("Unsupported video: " + file.getFileName()));
        if (!kind.isVideo()) {
            throw new BvxException("Not a video file: " + file.getFileName());
        }
        int width = 0;
        int height = 0;
        Duration duration = Duration.ZERO;
        double fps = 0;
        if (ffmpeg.available()) {
            FfmpegService.ProbeResult probe = ffmpeg.probe(file);
            width = probe.width();
            height = probe.height();
            duration = probe.duration();
            fps = probe.fps();
            if (duration.toSeconds() > config.mediaLimits().maxVideoDurationSeconds()) {
                throw new BvxException("Video duration " + duration.toSeconds() + "s exceeds limit "
                        + config.mediaLimits().maxVideoDurationSeconds() + "s");
            }
            if (width > config.mediaLimits().maxVideoWidth() || height > config.mediaLimits().maxVideoHeight()) {
                throw new BvxException("Video resolution " + width + "x" + height + " exceeds limit");
            }
        } else {
            logger.warning("Registered video " + file.getFileName() + " without probing because FFmpeg is missing");
        }
        VideoAssetImpl asset = new VideoAssetImpl(Names.assetName(file), file, kind.name().toLowerCase(), width, height, duration, fps);
        assets.put(asset.name(), asset);
        return asset;
    }

    @Override
    public Collection<VideoAsset> assets() {
        return List.copyOf(assets.values());
    }

    @Override
    public Collection<VideoScreen> screens() {
        return List.copyOf(screens.values());
    }

    @Override
    public Optional<VideoAsset> findAsset(String name) {
        return Optional.ofNullable(assets.get(name.toLowerCase()));
    }

    @Override
    public Optional<VideoScreen> findScreen(String id) {
        return Optional.ofNullable(screens.get(id));
    }

    @Override
    public CompletableFuture<VideoAsset> process(String name) {
        VideoAssetImpl asset = assets.get(name.toLowerCase());
        if (asset == null) {
            return CompletableFuture.failedFuture(new BvxException("Unknown video: " + name));
        }
        return scheduler.supplyAsync(() -> {
            if (!processing.tryAcquire()) {
                throw new BvxException("Too many videos processing at once (max " + config.maxConcurrentVideos() + ")");
            }
            try {
                ffmpeg.require();
                Path frames = layout.cache().resolve("videos").resolve(asset.name()).resolve("frames");
                Path audio = layout.cache().resolve("videos").resolve(asset.name()).resolve(asset.name() + ".ogg");
                ffmpeg.extractFrames(asset.file(), frames, config.defaultVideoFps(),
                        config.defaultVideoWidth(), config.defaultVideoHeight());
                try {
                    ffmpeg.extractAudio(asset.file(), audio);
                } catch (Exception ex) {
                    logger.warning("Audio extraction failed for " + asset.name() + ": " + ex.getMessage());
                    audio = null;
                }
                asset.markProcessed(frames, audio);
                return asset;
            } finally {
                processing.release();
            }
        });
    }

    @Override
    public VideoScreen createScreen(VideoScreenCreateRequest request) {
        if (!assets.containsKey(request.videoName().toLowerCase())) {
            throw new BvxException("Unknown video: " + request.videoName());
        }
        VideoScreenImpl screen = new VideoScreenImpl(request, events);
        screens.put(screen.id(), screen);
        platform.bindVideoScreen(screen);
        saveScreens();
        return screen;
    }

    @Override
    public boolean removeScreen(String id) {
        VideoScreenImpl removed = screens.remove(id);
        if (removed != null) {
            removed.stop();
            platform.unbindVideoScreen(id);
            saveScreens();
            return true;
        }
        return false;
    }

    @Override
    public Path contentDirectory() {
        return layout.videos();
    }

    public void shutdown() {
        screens.values().forEach(VideoScreenImpl::stop);
        saveScreens();
    }

    private void saveScreens() {
        try {
            Path file = layout.data().resolve("video-screens.json");
            List<Map<String, Object>> payload = new ArrayList<>();
            for (VideoScreenImpl screen : screens.values()) {
                payload.add(io.github.blazevortex.util.Maps.of(
                        "id", screen.id(),
                        "video", screen.videoName(),
                        "worldId", screen.worldId().toString(),
                        "world", screen.worldName(),
                        "x", screen.x(),
                        "y", screen.y(),
                        "z", screen.z(),
                        "yaw", screen.yaw(),
                        "width", screen.widthBlocks(),
                        "height", screen.heightBlocks(),
                        "fps", screen.fps(),
                        "looping", screen.looping()
                ));
            }
            Files.writeString(file, gson.toJson(payload), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.warning("Failed to persist video screens: " + ex.getMessage());
        }
    }

    private void loadScreens() {
        Path file = layout.data().resolve("video-screens.json");
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            List<Map<String, Object>> payload = gson.fromJson(
                    Files.readString(file, StandardCharsets.UTF_8),
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType()
            );
            if (payload == null) {
                return;
            }
            for (Map<String, Object> row : payload) {
                try {
                    VideoScreenCreateRequest request = VideoScreenCreateRequest.builder(
                                    String.valueOf(row.get("video")),
                                    java.util.UUID.fromString(String.valueOf(row.get("worldId"))),
                                    String.valueOf(row.get("world")),
                                    ((Number) row.get("x")).doubleValue(),
                                    ((Number) row.get("y")).doubleValue(),
                                    ((Number) row.get("z")).doubleValue()
                            )
                            .id(String.valueOf(row.get("id")))
                            .yaw(((Number) row.getOrDefault("yaw", 0)).floatValue())
                            .widthBlocks(((Number) row.getOrDefault("width", 5)).intValue())
                            .heightBlocks(((Number) row.getOrDefault("height", 3)).intValue())
                            .fps(((Number) row.getOrDefault("fps", 10)).intValue())
                            .looping(Boolean.parseBoolean(String.valueOf(row.getOrDefault("looping", true))))
                            .build();
                    VideoScreenImpl screen = new VideoScreenImpl(request, events);
                    screens.put(screen.id(), screen);
                    platform.bindVideoScreen(screen);
                } catch (Exception ex) {
                    logger.warning("Could not restore video screen: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            logger.warning("Failed to read video-screens.json: " + ex.getMessage());
        }
    }
}
