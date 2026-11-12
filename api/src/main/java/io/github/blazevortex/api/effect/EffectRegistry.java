package io.github.blazevortex.api.effect;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface EffectRegistry {

    Collection<BvxEffect> all();

    Optional<BvxEffect> find(String id);

    void play(String effectId, UUID worldId, double x, double y, double z);

    void playOn(String effectId, UUID playerId);
}
