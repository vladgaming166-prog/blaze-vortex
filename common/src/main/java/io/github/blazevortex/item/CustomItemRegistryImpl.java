package io.github.blazevortex.item;

import io.github.blazevortex.api.item.CustomItem;
import io.github.blazevortex.api.item.CustomItemRegistry;
import io.github.blazevortex.config.ConfigLoader;
import io.github.blazevortex.content.ContentLayout;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class CustomItemRegistryImpl implements CustomItemRegistry {

    private final ContentLayout layout;
    private final ConfigLoader loader;
    private final Logger logger;
    private final ConcurrentHashMap<String, CustomItem> items = new ConcurrentHashMap<>();

    public CustomItemRegistryImpl(ContentLayout layout, ConfigLoader loader, Logger logger) {
        this.layout = layout;
        this.loader = loader;
        this.logger = logger;
    }

    @Override
    public Collection<CustomItem> all() {
        return List.copyOf(items.values());
    }

    @Override
    public Optional<CustomItem> find(String id) {
        return Optional.ofNullable(items.get(id.toLowerCase()));
    }

    @Override
    public CustomItem register(CustomItem item) {
        items.put(item.id().toLowerCase(), item);
        return item;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reload() {
        items.clear();
        Path file = layout.dataFolder().resolve("items.yml");
        Map<String, Object> raw = loader.loadRaw(file, "config/items.yml");
        Object node = raw.get("items");
        if (!(node instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String id = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }
            try {
                List<String> lore = values.get("lore") instanceof List<?> list
                        ? list.stream().map(String::valueOf).toList()
                        : List.of();
                Integer cmd = values.get("custom-model-data") instanceof Number n ? n.intValue() : null;
                register(new CustomItemImpl(
                        id,
                        io.github.blazevortex.util.Maps.getString(values, "material", "PAPER"),
                        io.github.blazevortex.util.Maps.getString(values, "display-name", id),
                        lore,
                        values.get("model") == null ? null : String.valueOf(values.get("model")),
                        cmd
                ));
            } catch (Exception ex) {
                logger.warning("Failed to load item " + id + ": " + ex.getMessage());
            }
        }
    }
}
