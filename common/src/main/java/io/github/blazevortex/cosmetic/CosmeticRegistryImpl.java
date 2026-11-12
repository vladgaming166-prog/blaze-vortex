package io.github.blazevortex.cosmetic;

import io.github.blazevortex.api.cosmetic.Cosmetic;
import io.github.blazevortex.api.cosmetic.CosmeticRegistry;
import io.github.blazevortex.api.cosmetic.CosmeticType;
import io.github.blazevortex.config.ConfigLoader;
import io.github.blazevortex.content.ContentLayout;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class CosmeticRegistryImpl implements CosmeticRegistry {

    private final ContentLayout layout;
    private final ConfigLoader loader;
    private final Logger logger;
    private final ConcurrentHashMap<String, Cosmetic> cosmetics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Set<String>> equipped = new ConcurrentHashMap<>();

    public CosmeticRegistryImpl(ContentLayout layout, ConfigLoader loader, Logger logger) {
        this.layout = layout;
        this.loader = loader;
        this.logger = logger;
        reload();
    }

    @SuppressWarnings("unchecked")
    public void reload() {
        cosmetics.clear();
        Path file = layout.dataFolder().resolve("cosmetics.yml");
        Map<String, Object> raw = loader.loadRaw(file, "config/cosmetics.yml");
        Object node = raw.get("cosmetics");
        if (!(node instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String id = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }
            try {
                CosmeticType type = CosmeticType.valueOf(io.github.blazevortex.util.Maps.getString(values, "type", "HAT").toUpperCase());
                cosmetics.put(id, new CosmeticImpl(
                        id,
                        type,
                        io.github.blazevortex.util.Maps.getString(values, "display-name", id),
                        io.github.blazevortex.util.Maps.getString(values, "permission", "blazevortex.cosmetic." + id)
                ));
            } catch (Exception ex) {
                logger.warning("Failed to load cosmetic " + id + ": " + ex.getMessage());
            }
        }
    }

    @Override
    public Collection<Cosmetic> all() {
        return List.copyOf(cosmetics.values());
    }

    @Override
    public Optional<Cosmetic> find(String id) {
        return Optional.ofNullable(cosmetics.get(id));
    }

    @Override
    public void equip(UUID playerId, String cosmeticId) {
        if (!cosmetics.containsKey(cosmeticId)) {
            throw new io.github.blazevortex.api.BvxException("Unknown cosmetic: " + cosmeticId);
        }
        equipped.computeIfAbsent(playerId, key -> ConcurrentHashMap.newKeySet()).add(cosmeticId);
    }

    @Override
    public void unequip(UUID playerId, String cosmeticId) {
        Set<String> set = equipped.get(playerId);
        if (set != null) {
            set.remove(cosmeticId);
        }
    }

    @Override
    public Collection<String> equipped(UUID playerId) {
        return List.copyOf(equipped.getOrDefault(playerId, Set.of()));
    }
}
