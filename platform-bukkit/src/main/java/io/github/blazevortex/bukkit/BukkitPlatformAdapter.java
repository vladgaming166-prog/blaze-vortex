package io.github.blazevortex.bukkit;

import io.github.blazevortex.api.BlazeVortex;
import io.github.blazevortex.api.audio.SoundPlayRequest;
import io.github.blazevortex.api.audio.SoundSource;
import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.display.DisplayType;
import io.github.blazevortex.api.hologram.Hologram;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.bukkit.display.ArmorStandHologramSupport;
import io.github.blazevortex.bukkit.display.DisplayEntitySupport;
import io.github.blazevortex.bukkit.display.MapScreenSupport;
import io.github.blazevortex.core.BlazeVortexEngine;
import io.github.blazevortex.media.gif.GifRegistryImpl;
import io.github.blazevortex.media.image.ImageProcessor;
import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.PlatformAdapter;
import io.github.blazevortex.platform.PlatformCapabilities;
import io.github.blazevortex.platform.PlayerLocation;
import io.github.blazevortex.platform.PlayerLookTarget;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BukkitPlatformAdapter implements PlatformAdapter {

    private final JavaPlugin plugin;
    private final BukkitSchedulerAdapter scheduler;
    private final BukkitAudiences audiences;
    private final boolean nativeAdventure;
    private final boolean displayEntities;
    private final DisplayEntitySupport displayEntitiesSupport;
    private final ArmorStandHologramSupport armorStands;
    private final MapScreenSupport maps;
    private final Map<String, SoundSource> looping = new ConcurrentHashMap<>();
    private BlazeVortexEngine engine;

    public BukkitPlatformAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = new BukkitSchedulerAdapter(plugin);
        this.nativeAdventure = ClassProbe.present("net.kyori.adventure.Adventure");
        this.audiences = BukkitAudiences.create(plugin);
        this.displayEntities = ClassProbe.present("org.bukkit.entity.TextDisplay");
        this.maps = new MapScreenSupport(plugin);
        this.armorStands = new ArmorStandHologramSupport(plugin, line -> {
            if (engine == null) {
                return line;
            }
            return ArmorStandHologramSupport.legacy(engine.renderer().render(line, io.github.blazevortex.api.component.RenderContext.HOLOGRAM));
        });
        this.displayEntitiesSupport = displayEntities
                ? new DisplayEntitySupport(plugin, line -> engine == null ? Component.text(line)
                : engine.renderer().render(line, io.github.blazevortex.api.component.RenderContext.HOLOGRAM))
                : null;
    }

    public void attach(BlazeVortexEngine engine) {
        this.engine = engine;
    }

    public Plugin plugin() {
        return plugin;
    }

    @Override
    public String id() {
        return "bukkit";
    }

    @Override
    public String serverBrand() {
        return Bukkit.getName() + " " + Bukkit.getVersion();
    }

    @Override
    public String minecraftVersion() {
        return Bukkit.getBukkitVersion();
    }

    @Override
    public PlatformCapabilities capabilities() {
        return PlatformCapabilities.builder()
                .displayEntities(displayEntities)
                .itemDisplays(displayEntities)
                .textDisplays(displayEntities)
                .nativeAdventure(nativeAdventure)
                .mapViews(true)
                .customModelData(true)
                .itemModelComponents(ClassProbe.present("io.papermc.paper.datacomponent.DataComponentTypes"))
                .paperPluginLoader(ClassProbe.present("io.papermc.paper.plugin.loader.PluginLoader"))
                .brigadier(ClassProbe.present("com.mojang.brigadier.CommandDispatcher"))
                .resourcePackEvents(true)
                .build();
    }

    @Override
    public Logger logger() {
        return plugin.getLogger();
    }

    @Override
    public Path dataFolder() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public BvxScheduler scheduler() {
        return scheduler;
    }

    @Override
    public void sendMessage(UUID playerId, Component component) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            audience(player).sendMessage(component);
        }
    }

    @Override
    public void sendActionBar(UUID playerId, Component component) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            audience(player).sendActionBar(component);
        }
    }

    @Override
    public void sendTitle(UUID playerId, Component title, Component subtitle) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            audience(player).showTitle(Title.title(title, subtitle));
        }
    }

    @Override
    public void broadcast(Component component) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            audience(player).sendMessage(component);
        }
    }

    @Override
    public Optional<PlayerLookTarget> lookTarget(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return Optional.empty();
        }
        Block target = player.getTargetBlockExact(6);
        Location loc = target != null ? target.getLocation().add(0.5, 1, 0.5) : player.getLocation();
        return Optional.of(new PlayerLookTarget(
                player.getUniqueId(),
                loc.getWorld().getUID(),
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch(),
                target != null
        ));
    }

    @Override
    public Optional<PlayerLocation> locationOf(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return Optional.empty();
        }
        Location loc = player.getLocation();
        return Optional.of(new PlayerLocation(
                player.getUniqueId(), loc.getWorld().getUID(), loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()
        ));
    }

    @Override
    public BvxDisplay spawnDisplay(DisplayCreateRequest request) {
        try {
            // Always paint pixels onto MapViews in item frames. ItemDisplay of an empty
            // FILLED_MAP only shows the blank map item model, which is what players saw.
            if (request.type() == DisplayType.GIF) {
                var sequence = ((GifRegistryImpl) engine.gifs()).frames(request.assetName());
                java.util.List<BufferedImage> frames = sequence.frames().stream()
                        .map(io.github.blazevortex.media.gif.GifFrame::image)
                        .toList();
                int[] delays = sequence.delaysMs().stream().mapToInt(Integer::intValue).toArray();
                return maps.spawnAnimated(request, frames, delays);
            }
            return maps.spawnImage(request, loadImage(request));
        } catch (Exception ex) {
            throw new io.github.blazevortex.api.BvxException("Failed to spawn display: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void despawnDisplay(String displayId) {
        if (displayEntitiesSupport != null) {
            displayEntitiesSupport.despawnDisplay(displayId);
        }
        maps.unbind(displayId);
    }

    @Override
    public Hologram spawnHologram(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines) {
        if (displayEntitiesSupport != null) {
            return displayEntitiesSupport.spawnHologram(id, worldId, worldName, x, y, z, lines);
        }
        return armorStands.spawn(id, worldId, worldName, x, y, z, lines);
    }

    @Override
    public void despawnHologram(String id) {
        if (displayEntitiesSupport != null) {
            displayEntitiesSupport.despawnHologram(id);
        }
        armorStands.despawn(id);
    }

    @Override
    public void playSound(SoundPlayRequest request) {
        Sound sound = Sound.sound(Key.key("blazevortex", request.soundName()), Sound.Source.MASTER, request.volume(), request.pitch());
        if (request.playerId() != null) {
            Player player = Bukkit.getPlayer(request.playerId());
            if (player != null) {
                audience(player).playSound(sound);
            }
            return;
        }
        if (request.global()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                audience(player).playSound(sound);
            }
            return;
        }
        if (request.worldId() != null && request.x() != null) {
            World world = Bukkit.getWorld(request.worldId());
            if (world != null) {
                Location location = new Location(world, request.x(), request.y(), request.z());
                world.playSound(location, "blazevortex:" + request.soundName(), request.volume(), request.pitch());
            }
        }
    }

    @Override
    public void stopSound(String namespacedKey) {
        Key key = Key.key(namespacedKey.contains(":") ? namespacedKey : "blazevortex:" + namespacedKey);
        for (Player player : Bukkit.getOnlinePlayers()) {
            audience(player).stopSound(Sound.sound(key, Sound.Source.MASTER, 1f, 1f));
        }
    }

    @Override
    public void attachSoundSource(SoundSource source) {
        looping.put(source.id(), source);
        playSound(SoundPlayRequest.of(source.soundName())
                .location(source.worldId(), source.x(), source.y(), source.z())
                .volume(source.volume())
                .pitch(source.pitch())
                .looping(source.looping())
                .build());
    }

    @Override
    public void detachSoundSource(String sourceId) {
        SoundSource source = looping.remove(sourceId);
        if (source != null) {
            stopSound(source.soundName());
        }
    }

    @Override
    public void bindVideoScreen(VideoScreen screen) {
        maps.bindVideo(screen, () -> currentVideoFrame(screen));
    }

    @Override
    public void unbindVideoScreen(String screenId) {
        maps.unbind(screenId);
    }

    @Override
    public void sendResourcePack(ResourcePackBuildResult pack, boolean required) {
        if (pack.packFile() == null) {
            return;
        }
        String url = engine != null ? engine.config().packUrl() : "";
        if (url == null || url.isBlank()) {
            plugin.getLogger().warning("Resource pack built at " + pack.packFile()
                    + " but resource-pack.url is empty, so it cannot be pushed to clients.");
            return;
        }
        byte[] hash = HexFormat.of().parseHex(pack.sha1());
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.setResourcePack(URI.create(url).toString(), hash, required);
            } catch (Exception ex) {
                player.setResourcePack(url, pack.sha1());
            }
        }
    }

    @Override
    public void playParticle(String particle, UUID worldId, String worldName, double x, double y, double z, int count, double extra) {
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            world = Bukkit.getWorld(worldName);
        }
        if (world == null) {
            return;
        }
        try {
            org.bukkit.Particle type = org.bukkit.Particle.valueOf(particle.toUpperCase(java.util.Locale.ROOT));
            world.spawnParticle(type, x, y, z, Math.max(1, count), extra, extra, extra, extra);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Unknown particle: " + particle);
        }
    }

    @Override
    public void applyChatFormatting(BlazeVortex engine) {
        this.engine = (BlazeVortexEngine) engine;
        plugin.getServer().getPluginManager().registerEvents(new io.github.blazevortex.bukkit.listener.ChatEmojiListener(this.engine, audiences, plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new io.github.blazevortex.bukkit.listener.JoinPackListener(this.engine, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new io.github.blazevortex.bukkit.gui.GuiListener(), plugin);
        scheduler.runSyncRepeating(this::tickLoopingSounds, 40, 100);
        scheduler.runSyncRepeating(this::tickHolograms, 40, Math.max(5, this.engine.config().hologramUpdateTicks()));
    }

    @Override
    public void shutdownWorldObjects() {
        maps.shutdown();
        armorStands.shutdown();
        if (displayEntitiesSupport != null) {
            displayEntitiesSupport.shutdown();
        }
        if (audiences != null) {
            audiences.close();
        }
    }

    private void tickLoopingSounds() {
        for (SoundSource source : looping.values()) {
            if (source.looping() && source.playing()) {
                playSound(SoundPlayRequest.of(source.soundName())
                        .location(source.worldId(), source.x(), source.y(), source.z())
                        .volume(source.volume())
                        .pitch(source.pitch())
                        .build());
            }
        }
    }

    private void tickHolograms() {
        if (engine == null) {
            return;
        }
        engine.holograms().all().forEach(hologram -> {
            boolean animated = hologram.lines().stream().anyMatch(line ->
                    engine.emojis().all().stream().anyMatch(emoji ->
                            emoji.animated() && line.contains(emoji.tag())));
            if (animated) {
                hologram.setLines(hologram.lines());
            }
        });
    }

    private BufferedImage loadImage(DisplayCreateRequest request) throws Exception {
        if (engine == null) {
            throw new io.github.blazevortex.api.BvxException("Cannot spawn a display before the engine is attached");
        }
        Path file = engine.images().find(request.assetName()).map(io.github.blazevortex.api.image.BvxImage::file)
                .or(() -> engine.emojis().find(request.assetName()).map(io.github.blazevortex.api.emoji.Emoji::file))
                .orElseThrow(() -> new io.github.blazevortex.api.BvxException(
                        "Unknown image '" + request.assetName() + "'. Put a PNG/JPG/WEBP in plugins/BlazeVortex/content/images/ (or content/emojis/) then /bvx reload."));
        return new ImageProcessor(engine.config().mediaLimits(), 8).read(file);
    }

    private BufferedImage currentVideoFrame(VideoScreen screen) {
        if (engine == null) {
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }
        var asset = engine.videos().findAsset(screen.videoName()).orElse(null);
        if (asset == null || !(asset instanceof io.github.blazevortex.media.video.VideoAssetImpl impl) || impl.frameDirectory().isEmpty()) {
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }
        try {
            java.nio.file.Path dir = impl.frameDirectory().get();
            try (var stream = java.nio.file.Files.list(dir)) {
                java.util.List<java.nio.file.Path> frames = stream.sorted().toList();
                if (frames.isEmpty()) {
                    return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
                }
                int index = 0;
                if (screen instanceof io.github.blazevortex.media.video.VideoScreenImpl implScreen) {
                    index = implScreen.currentFrame(frames.size(), screen.fps());
                }
                return javax.imageio.ImageIO.read(frames.get(index).toFile());
            }
        } catch (Exception ex) {
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        }
    }

    private Audience audience(Player player) {
        return audiences.player(player);
    }
}
