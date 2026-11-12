package io.github.blazevortex.platform;

import io.github.blazevortex.api.BlazeVortex;
import io.github.blazevortex.api.audio.SoundPlayRequest;
import io.github.blazevortex.api.audio.SoundSource;
import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.hologram.Hologram;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.api.video.VideoScreen;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Version-specific bridge between the common engine and a Minecraft server.
 *
 * <p>Implementations must not be loaded unless their target APIs are present.
 */
public interface PlatformAdapter {

    String id();

    String serverBrand();

    String minecraftVersion();

    PlatformCapabilities capabilities();

    Logger logger();

    Path dataFolder();

    BvxScheduler scheduler();

    void sendMessage(UUID playerId, Component component);

    void sendActionBar(UUID playerId, Component component);

    void sendTitle(UUID playerId, Component title, Component subtitle);

    void broadcast(Component component);

    Optional<PlayerLookTarget> lookTarget(UUID playerId);

    Optional<PlayerLocation> locationOf(UUID playerId);

    BvxDisplay spawnDisplay(DisplayCreateRequest request);

    void despawnDisplay(String displayId);

    Hologram spawnHologram(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines);

    void despawnHologram(String id);

    void playSound(SoundPlayRequest request);

    void stopSound(String namespacedKey);

    void attachSoundSource(SoundSource source);

    void detachSoundSource(String sourceId);

    void bindVideoScreen(VideoScreen screen);

    void unbindVideoScreen(String screenId);

    void sendResourcePack(ResourcePackBuildResult pack, boolean required);

    void applyChatFormatting(BlazeVortex engine);

    void playParticle(String particle, UUID worldId, String worldName, double x, double y, double z, int count, double extra);

    void shutdownWorldObjects();
}
