package io.github.blazevortex.effect;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.effect.BvxEffect;
import io.github.blazevortex.api.effect.EffectRegistry;
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

public final class EffectRegistryImpl implements EffectRegistry {

    private final ContentLayout layout;
    private final ConfigLoader loader;
    private final Logger logger;
    private final PlatformAdapter platform;
    private final ConcurrentHashMap<String, BvxEffect> effects = new ConcurrentHashMap<>();

    public EffectRegistryImpl(ContentLayout layout, ConfigLoader loader, Logger logger, PlatformAdapter platform) {
        this.layout = layout;
        this.loader = loader;
        this.logger = logger;
        this.platform = platform;
        reload();
    }

    public void reload() {
        effects.clear();
        Path file = layout.dataFolder().resolve("effects.yml");
        Map<String, Object> raw = loader.loadRaw(file, "config/effects.yml");
        Object node = raw.get("effects");
        if (!(node instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String id = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> values)) {
                continue;
            }
            try {
                effects.put(id, new EffectImpl(
                        id,
                        io.github.blazevortex.util.Maps.getString(values, "particle", "FLAME"),
                        values.get("duration-ticks") instanceof Number n ? n.intValue() : 40,
                        values.get("intensity") instanceof Number n ? n.doubleValue() : 1.0,
                        values.get("max-particles") instanceof Number n ? n.intValue() : 32
                ));
            } catch (Exception ex) {
                logger.warning("Failed to load effect " + id + ": " + ex.getMessage());
            }
        }
    }

    @Override
    public Collection<BvxEffect> all() {
        return List.copyOf(effects.values());
    }

    @Override
    public Optional<BvxEffect> find(String id) {
        return Optional.ofNullable(effects.get(id));
    }

    @Override
    public void play(String effectId, UUID worldId, double x, double y, double z) {
        BvxEffect effect = effects.get(effectId);
        if (effect == null) {
            throw new BvxException("Unknown effect: " + effectId);
        }
        platform.playParticle(effect.particle(), worldId, worldId.toString(), x, y, z, effect.maxParticles(), effect.intensity());
    }

    @Override
    public void playOn(String effectId, UUID playerId) {
        BvxEffect effect = effects.get(effectId);
        if (effect == null) {
            throw new BvxException("Unknown effect: " + effectId);
        }
        platform.locationOf(playerId).ifPresent(loc ->
                platform.playParticle(effect.particle(), loc.worldId(), loc.worldName(), loc.x(), loc.y(), loc.z(),
                        effect.maxParticles(), effect.intensity()));
    }
}
