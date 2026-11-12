package io.github.blazevortex.cosmetic;

import io.github.blazevortex.api.cosmetic.Cosmetic;
import io.github.blazevortex.api.cosmetic.CosmeticType;

public record CosmeticImpl(String id, CosmeticType type, String displayName, String permission) implements Cosmetic {
}
