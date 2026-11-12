package io.github.blazevortex.api.cosmetic;

public interface Cosmetic {

    String id();

    CosmeticType type();

    String displayName();

    String permission();
}
