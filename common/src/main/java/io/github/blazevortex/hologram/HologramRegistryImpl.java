package io.github.blazevortex.hologram;

import io.github.blazevortex.api.hologram.Hologram;
import io.github.blazevortex.api.hologram.HologramRegistry;
import io.github.blazevortex.config.ConfigLoader;
import io.github.blazevortex.content.ContentLayout;
import io.github.blazevortex.platform.PlatformAdapter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class HologramRegistryImpl implements HologramRegistry {

    private final PlatformAdapter platform;
    private final ContentLayout layout;
    private final ConfigLoader loader;
    private final Logger logger;
    private final ConcurrentHashMap<String, Hologram> holograms = new ConcurrentHashMap<>();

    public HologramRegistryImpl(PlatformAdapter platform, ContentLayout layout, ConfigLoader loader, Logger logger) {
        this.platform = platform;
        this.layout = layout;
        this.loader = loader;
        this.logger = logger;
    }

    @Override
    public Collection<Hologram> all() {
        return List.copyOf(holograms.values());
    }

    @Override
    public Optional<Hologram> find(String id) {
        return Optional.ofNullable(holograms.get(id));
    }

    @Override
    public Hologram create(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines) {
        Hologram hologram = platform.spawnHologram(id, worldId, worldName, x, y, z, lines);
        holograms.put(id, hologram);
        return hologram;
    }

    @Override
    public boolean remove(String id) {
        Hologram removed = holograms.remove(id);
        if (removed != null) {
            platform.despawnHologram(id);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reload() {
        for (String id : List.copyOf(holograms.keySet())) {
            platform.despawnHologram(id);
        }
        holograms.clear();
        Path file = layout.dataFolder().resolve("holograms.yml");
        Map<String, Object> raw = loader.loadRaw(file, "config/holograms.yml");
        Object node = raw.get("holograms");
        if (!(node instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String id = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }
            try {
                Map<String, Object> loc = (Map<String, Object>) values.get("location");
                Object lineNode = values.get("lines");
                List<String> lines = lineNode instanceof List<?> list
                        ? list.stream().map(String::valueOf).toList()
                        : List.of();
                String world = String.valueOf(loc.get("world"));
                UUID worldId = UUID.nameUUIDFromBytes(world.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                create(id, worldId, world,
                        ((Number) loc.get("x")).doubleValue(),
                        ((Number) loc.get("y")).doubleValue(),
                        ((Number) loc.get("z")).doubleValue(),
                        lines);
            } catch (Exception ex) {
                logger.warning("Failed to load hologram " + id + ": " + ex.getMessage());
            }
        }
    }
}
