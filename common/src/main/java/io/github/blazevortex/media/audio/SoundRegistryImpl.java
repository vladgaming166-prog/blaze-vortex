package io.github.blazevortex.media.audio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.audio.SoundAsset;
import io.github.blazevortex.api.audio.SoundPlayRequest;
import io.github.blazevortex.api.audio.SoundRegistry;
import io.github.blazevortex.api.audio.SoundSource;
import io.github.blazevortex.api.audio.SoundSourceCreateRequest;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.event.SoundPlayEvent;
import io.github.blazevortex.config.BvxConfig;
import io.github.blazevortex.content.AssetDiscovery;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.media.ffmpeg.FfmpegService;
import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.PlatformAdapter;
import io.github.blazevortex.security.MagicBytes;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.util.Names;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundRegistryImpl implements SoundRegistry {

    private final ContentLayout layout;
    private final BvxConfig config;
    private final FfmpegService ffmpeg;
    private final BvxScheduler scheduler;
    private final BvxEventBus events;
    private final Logger logger;
    private final PlatformAdapter platform;
    private final ConcurrentHashMap<String, SoundAssetImpl> assets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SoundSourceImpl> sources = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SoundRegistryImpl(
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
    }

    public void discover() {
        try {
            for (Path file : AssetDiscovery.discover(layout.sounds(), AssetDiscovery.kinds(
                    MediaKind.MP3, MediaKind.WAV, MediaKind.OGG))) {
                try {
                    registerFile(file);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Sound '" + file.getFileName() + "' rejected: " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Sound discovery failed: " + ex.getMessage(), ex);
        }
        loadSources();
    }

    public SoundAssetImpl registerFile(Path file) throws IOException {
        MediaKind kind = MagicBytes.detect(file).orElseThrow(() ->
                new BvxException("Unsupported audio: " + file.getFileName()));
        if (!kind.isAudio()) {
            throw new BvxException("Not an audio file: " + file.getFileName());
        }
        Duration duration = Duration.ZERO;
        if (ffmpeg.available()) {
            duration = ffmpeg.probe(file).duration();
            if (duration.toSeconds() > config.mediaLimits().maxAudioDurationSeconds()) {
                throw new BvxException("Audio duration exceeds limit");
            }
        }
        Path ogg = null;
        if (kind == MediaKind.OGG) {
            ogg = layout.cache().resolve("sounds").resolve(Names.assetName(file) + ".ogg");
            Files.createDirectories(ogg.getParent());
            Files.copy(file, ogg, StandardCopyOption.REPLACE_EXISTING);
        }
        SoundAssetImpl asset = new SoundAssetImpl(Names.assetName(file), file, kind.name().toLowerCase(), duration, ogg);
        assets.put(asset.name(), asset);
        return asset;
    }

    @Override
    public Collection<SoundAsset> assets() {
        return List.copyOf(assets.values());
    }

    @Override
    public Collection<SoundSource> sources() {
        return List.copyOf(sources.values());
    }

    @Override
    public Optional<SoundAsset> findAsset(String name) {
        return Optional.ofNullable(assets.get(name.toLowerCase()));
    }

    @Override
    public Optional<SoundSource> findSource(String id) {
        return Optional.ofNullable(sources.get(id));
    }

    @Override
    public CompletableFuture<SoundAsset> convert(String name) {
        SoundAssetImpl asset = assets.get(name.toLowerCase());
        if (asset == null) {
            return CompletableFuture.failedFuture(new BvxException("Unknown sound: " + name));
        }
        return scheduler.supplyAsync(() -> {
            try {
                ffmpeg.require();
                Path ogg = layout.cache().resolve("sounds").resolve(asset.name() + ".ogg");
                if (asset.format().equals("ogg")) {
                    Files.createDirectories(ogg.getParent());
                    Files.copy(asset.sourceFile(), ogg, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    ffmpeg.convertToOgg(asset.sourceFile(), ogg);
                }
                asset.setOggFile(ogg);
                return asset;
            } catch (IOException ex) {
                throw new BvxException("Sound conversion failed: " + ex.getMessage(), ex);
            }
        });
    }

    @Override
    public void play(SoundPlayRequest request) {
        SoundPlayEvent event = events.fire(new SoundPlayEvent(request));
        if (event.isCancelled()) {
            return;
        }
        if (!assets.containsKey(request.soundName().toLowerCase())) {
            throw new BvxException("Unknown sound: " + request.soundName());
        }
        platform.playSound(request);
    }

    @Override
    public void stop(String soundName) {
        SoundAssetImpl asset = assets.get(soundName.toLowerCase());
        if (asset != null) {
            platform.stopSound(asset.namespacedKey());
        }
    }

    @Override
    public void stopFor(UUID playerId, String soundName) {
        stop(soundName);
    }

    @Override
    public SoundSource putSource(SoundSourceCreateRequest request) {
        if (!assets.containsKey(request.soundName().toLowerCase())) {
            throw new BvxException("Unknown sound: " + request.soundName());
        }
        SoundSourceImpl source = new SoundSourceImpl(request, platform::attachSoundSource, src -> platform.detachSoundSource(src.id()));
        sources.put(source.id(), source);
        source.play();
        saveSources();
        return source;
    }

    @Override
    public boolean removeSource(String id) {
        SoundSourceImpl removed = sources.remove(id);
        if (removed != null) {
            removed.stop();
            saveSources();
            return true;
        }
        return false;
    }

    @Override
    public Path contentDirectory() {
        return layout.sounds();
    }

    public void shutdown() {
        sources.values().forEach(SoundSourceImpl::stop);
        saveSources();
    }

    private void saveSources() {
        try {
            List<Map<String, Object>> payload = new ArrayList<>();
            for (SoundSourceImpl source : sources.values()) {
                payload.add(io.github.blazevortex.util.Maps.of(
                        "id", source.id(),
                        "sound", source.soundName(),
                        "worldId", source.worldId().toString(),
                        "world", source.worldName(),
                        "x", source.x(),
                        "y", source.y(),
                        "z", source.z(),
                        "volume", source.volume(),
                        "pitch", source.pitch(),
                        "radius", source.radius(),
                        "looping", source.looping()
                ));
            }
            Files.writeString(layout.data().resolve("sound-sources.json"), gson.toJson(payload), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.warning("Failed to persist sound sources: " + ex.getMessage());
        }
    }

    private void loadSources() {
        Path file = layout.data().resolve("sound-sources.json");
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
                    SoundSourceCreateRequest request = SoundSourceCreateRequest.builder(
                                    String.valueOf(row.get("sound")),
                                    java.util.UUID.fromString(String.valueOf(row.get("worldId"))),
                                    String.valueOf(row.get("world")),
                                    ((Number) row.get("x")).doubleValue(),
                                    ((Number) row.get("y")).doubleValue(),
                                    ((Number) row.get("z")).doubleValue()
                            )
                            .id(String.valueOf(row.get("id")))
                            .volume(((Number) row.getOrDefault("volume", 1)).floatValue())
                            .pitch(((Number) row.getOrDefault("pitch", 1)).floatValue())
                            .radius(((Number) row.getOrDefault("radius", 16)).doubleValue())
                            .looping(Boolean.parseBoolean(String.valueOf(row.getOrDefault("looping", true))))
                            .build();
                    SoundSourceImpl source = new SoundSourceImpl(request, platform::attachSoundSource, src -> platform.detachSoundSource(src.id()));
                    sources.put(source.id(), source);
                    source.play();
                } catch (Exception ex) {
                    logger.warning("Could not restore sound source: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            logger.warning("Failed to read sound-sources.json: " + ex.getMessage());
        }
    }
}
