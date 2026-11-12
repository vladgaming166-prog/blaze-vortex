package io.github.blazevortex.bukkit.display;

import io.github.blazevortex.api.hologram.Hologram;
import io.github.blazevortex.hologram.HologramImpl;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 1.19.0-1.19.3 hologram fallback using invisible armor stands. Safe on every supported Bukkit version.
 */
public final class ArmorStandHologramSupport {

    private final Plugin plugin;
    private final org.bukkit.NamespacedKey key;
    private final Map<String, List<UUID>> entities = new ConcurrentHashMap<>();
    private final Function<String, String> lineRenderer;

    public ArmorStandHologramSupport(Plugin plugin, Function<String, String> lineRenderer) {
        this.plugin = plugin;
        this.key = new org.bukkit.NamespacedKey(plugin, "hologram");
        this.lineRenderer = lineRenderer;
    }

    public Hologram spawn(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines) {
        World world = worldBy(worldId, worldName);
        despawn(id);
        List<UUID> spawned = new ArrayList<>();
        double cursor = y;
        for (String line : lines) {
            Location location = new Location(world, x, cursor, z);
            ArmorStand stand = world.spawn(location, ArmorStand.class, entity -> {
                entity.setInvisible(true);
                entity.setMarker(true);
                entity.setGravity(false);
                entity.setCustomNameVisible(true);
                entity.setCustomName(lineRenderer.apply(line));
                entity.setPersistent(true);
                entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            });
            spawned.add(stand.getUniqueId());
            cursor -= 0.28;
        }
        entities.put(id, spawned);
        return new HologramImpl(id, worldId, worldName, x, y, z, lines, this::refresh, this::despawn);
    }

    public void refresh(HologramImpl hologram) {
        spawn(hologram.id(), hologram.worldId(), hologram.worldName(), hologram.x(), hologram.y(), hologram.z(), hologram.lines());
    }

    public void despawn(String id) {
        List<UUID> spawned = entities.remove(id);
        if (spawned == null) {
            return;
        }
        for (UUID uuid : spawned) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public void shutdown() {
        for (String id : List.copyOf(entities.keySet())) {
            despawn(id);
        }
    }

    private World worldBy(UUID worldId, String worldName) {
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            world = Bukkit.getWorld(worldName);
        }
        if (world == null) {
            throw new IllegalStateException("World not loaded: " + worldName);
        }
        return world;
    }

    public static String legacy(net.kyori.adventure.text.Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}
