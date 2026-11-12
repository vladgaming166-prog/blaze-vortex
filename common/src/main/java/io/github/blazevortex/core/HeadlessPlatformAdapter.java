package io.github.blazevortex.core;

import io.github.blazevortex.api.BlazeVortex;
import io.github.blazevortex.api.audio.SoundPlayRequest;
import io.github.blazevortex.api.audio.SoundSource;
import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.hologram.Hologram;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.display.MemoryDisplay;
import io.github.blazevortex.hologram.HologramImpl;
import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.PlatformAdapter;
import io.github.blazevortex.platform.PlatformCapabilities;
import io.github.blazevortex.platform.PlayerLocation;
import io.github.blazevortex.platform.PlayerLookTarget;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Platform used by tests and by the engine when no Minecraft server is present.
 */
public final class HeadlessPlatformAdapter implements PlatformAdapter {

    private final Path dataFolder;
    private final Logger logger;
    private final BvxScheduler scheduler;
    private final Map<String, BvxDisplay> displays = new ConcurrentHashMap<>();
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();

    public HeadlessPlatformAdapter(Path dataFolder, Logger logger, BvxScheduler scheduler) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.scheduler = scheduler;
    }

    @Override
    public String id() {
        return "headless";
    }

    @Override
    public String serverBrand() {
        return "headless";
    }

    @Override
    public String minecraftVersion() {
        return "1.21.1";
    }

    @Override
    public PlatformCapabilities capabilities() {
        return PlatformCapabilities.builder().mapViews(false).customModelData(false).build();
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public BvxScheduler scheduler() {
        return scheduler;
    }

    @Override
    public void sendMessage(UUID playerId, Component component) {
    }

    @Override
    public void sendActionBar(UUID playerId, Component component) {
    }

    @Override
    public void sendTitle(UUID playerId, Component title, Component subtitle) {
    }

    @Override
    public void broadcast(Component component) {
    }

    @Override
    public Optional<PlayerLookTarget> lookTarget(UUID playerId) {
        return Optional.empty();
    }

    @Override
    public Optional<PlayerLocation> locationOf(UUID playerId) {
        return Optional.empty();
    }

    @Override
    public BvxDisplay spawnDisplay(DisplayCreateRequest request) {
        MemoryDisplay display = new MemoryDisplay(request, displays::remove);
        displays.put(display.id(), display);
        return display;
    }

    @Override
    public void despawnDisplay(String displayId) {
        displays.remove(displayId);
    }

    @Override
    public Hologram spawnHologram(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines) {
        HologramImpl hologram = new HologramImpl(id, worldId, worldName, x, y, z, lines, h -> {
        }, holograms::remove);
        holograms.put(id, hologram);
        return hologram;
    }

    @Override
    public void despawnHologram(String id) {
        holograms.remove(id);
    }

    @Override
    public void playSound(SoundPlayRequest request) {
    }

    @Override
    public void stopSound(String namespacedKey) {
    }

    @Override
    public void attachSoundSource(SoundSource source) {
    }

    @Override
    public void detachSoundSource(String sourceId) {
    }

    @Override
    public void bindVideoScreen(VideoScreen screen) {
    }

    @Override
    public void unbindVideoScreen(String screenId) {
    }

    @Override
    public void sendResourcePack(ResourcePackBuildResult pack, boolean required) {
    }

    @Override
    public void applyChatFormatting(BlazeVortex engine) {
    }

    @Override
    public void playParticle(String particle, UUID worldId, String worldName, double x, double y, double z, int count, double extra) {
    }

    @Override
    public void shutdownWorldObjects() {
        displays.clear();
        holograms.clear();
    }
}
