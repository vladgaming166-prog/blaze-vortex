package io.github.blazevortex.bukkit.display;

import io.github.blazevortex.api.display.BvxDisplay;
import io.github.blazevortex.api.display.DisplayCreateRequest;
import io.github.blazevortex.api.hologram.Hologram;
import io.github.blazevortex.display.MemoryDisplay;
import io.github.blazevortex.hologram.HologramImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Isolated 1.19.4+ display-entity implementation. This class is only loaded after
 * {@code org.bukkit.entity.TextDisplay} is confirmed present.
 */
public final class DisplayEntitySupport {

    private final Plugin plugin;
    private final org.bukkit.NamespacedKey key;
    private final Function<String, Component> lineRenderer;
    private final Map<String, UUID> displays = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> holograms = new ConcurrentHashMap<>();

    public DisplayEntitySupport(Plugin plugin, Function<String, Component> lineRenderer) {
        this.plugin = plugin;
        this.key = new org.bukkit.NamespacedKey(plugin, "display");
        this.lineRenderer = lineRenderer;
    }

    public BvxDisplay spawnDisplay(DisplayCreateRequest request, ItemStack stack) {
        World world = worldBy(request.worldId(), request.worldName());
        Location location = new Location(world, request.x(), request.y(), request.z(), request.yaw(), request.pitch());
        ItemDisplay entity = world.spawn(location, ItemDisplay.class, display -> {
            display.setItemStack(stack);
            display.setBillboard(Display.Billboard.FIXED);
            display.setViewRange(request.viewDistance());
            display.setPersistent(true);
            display.getPersistentDataContainer().set(key, PersistentDataType.STRING, request.id());
            display.setTransformation(new Transformation(
                    new Vector3f(),
                    new AxisAngle4f(),
                    new Vector3f(request.width(), request.height(), 0.1f),
                    new AxisAngle4f()
            ));
        });
        displays.put(request.id(), entity.getUniqueId());
        return new MemoryDisplay(request, this::despawnDisplay);
    }

    public void despawnDisplay(String id) {
        UUID uuid = displays.remove(id);
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public Hologram spawnHologram(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines) {
        World world = worldBy(worldId, worldName);
        despawnHologram(id);
        List<UUID> spawned = new ArrayList<>();
        double cursor = y;
        for (String line : lines) {
            Location location = new Location(world, x, cursor, z);
            TextDisplay display = world.spawn(location, TextDisplay.class, entity -> {
                entity.text(lineRenderer.apply(line));
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setSeeThrough(true);
                entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                entity.setPersistent(true);
                entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            });
            spawned.add(display.getUniqueId());
            cursor -= 0.28;
        }
        holograms.put(id, spawned);
        return new HologramImpl(id, worldId, worldName, x, y, z, lines, h ->
                spawnHologram(h.id(), h.worldId(), h.worldName(), h.x(), h.y(), h.z(), h.lines()), this::despawnHologram);
    }

    public void despawnHologram(String id) {
        List<UUID> spawned = holograms.remove(id);
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
        for (String id : List.copyOf(displays.keySet())) {
            despawnDisplay(id);
        }
        for (String id : List.copyOf(holograms.keySet())) {
            despawnHologram(id);
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
}
