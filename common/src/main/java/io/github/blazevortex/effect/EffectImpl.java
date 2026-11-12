package io.github.blazevortex.effect;

import io.github.blazevortex.api.effect.BvxEffect;

public record EffectImpl(String id, String particle, int durationTicks, double intensity, int maxParticles)
        implements BvxEffect {
}
