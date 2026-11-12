package io.github.blazevortex.platform;

import java.util.UUID;

public record PlayerLocation(
        UUID playerId,
        UUID worldId,
        String worldName,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}
