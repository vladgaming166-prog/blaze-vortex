package io.github.blazevortex.api.display;

import java.util.Objects;
import java.util.UUID;

public final class DisplayCreateRequest {

    private final String id;
    private final DisplayType type;
    private final String assetName;
    private final UUID worldId;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final float width;
    private final float height;
    private final int viewDistance;

    private DisplayCreateRequest(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.assetName = builder.assetName;
        this.worldId = builder.worldId;
        this.worldName = builder.worldName;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.yaw = builder.yaw;
        this.pitch = builder.pitch;
        this.width = builder.width;
        this.height = builder.height;
        this.viewDistance = builder.viewDistance;
    }

    public String id() {
        return id;
    }

    public DisplayType type() {
        return type;
    }

    public String assetName() {
        return assetName;
    }

    public UUID worldId() {
        return worldId;
    }

    public String worldName() {
        return worldName;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public int viewDistance() {
        return viewDistance;
    }

    public static Builder builder(DisplayType type, String assetName, UUID worldId, String worldName, double x, double y, double z) {
        return new Builder(type, assetName, worldId, worldName, x, y, z);
    }

    public static final class Builder {
        private String id;
        private final DisplayType type;
        private final String assetName;
        private final UUID worldId;
        private final String worldName;
        private final double x;
        private final double y;
        private final double z;
        private float yaw;
        private float pitch;
        private float width = 2.0f;
        private float height = 2.0f;
        private int viewDistance = 64;

        private Builder(DisplayType type, String assetName, UUID worldId, String worldName, double x, double y, double z) {
            this.type = Objects.requireNonNull(type, "type");
            this.assetName = Objects.requireNonNull(assetName, "assetName");
            this.worldId = Objects.requireNonNull(worldId, "worldId");
            this.worldName = Objects.requireNonNull(worldName, "worldName");
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder yaw(float yaw) {
            this.yaw = yaw;
            return this;
        }

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder width(float width) {
            this.width = width;
            return this;
        }

        public Builder height(float height) {
            this.height = height;
            return this;
        }

        public Builder viewDistance(int viewDistance) {
            this.viewDistance = viewDistance;
            return this;
        }

        public DisplayCreateRequest build() {
            if (id == null || id.isBlank()) {
                id = type.name().toLowerCase() + "-" + Integer.toHexString(Objects.hash(assetName, worldName, x, y, z));
            }
            return new DisplayCreateRequest(this);
        }
    }
}
