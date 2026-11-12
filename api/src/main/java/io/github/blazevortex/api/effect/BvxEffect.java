package io.github.blazevortex.api.effect;

public interface BvxEffect {

    String id();

    String particle();

    int durationTicks();

    double intensity();

    int maxParticles();
}
