package io.github.blazevortex.api.video;

import java.util.Objects;
import java.util.UUID;

public final class VideoScreenCreateRequest {

    private final String id;
    private final String videoName;
    private final UUID worldId;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final int widthBlocks;
    private final int heightBlocks;
    private final int fps;
    private final boolean looping;

    private VideoScreenCreateRequest(Builder builder) {
        this.id = builder.id;
        this.videoName = builder.videoName;
        this.worldId = builder.worldId;
        this.worldName = builder.worldName;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.yaw = builder.yaw;
        this.widthBlocks = builder.widthBlocks;
        this.heightBlocks = builder.heightBlocks;
        this.fps = builder.fps;
        this.looping = builder.looping;
    }

    public String id() {
        return id;
    }

    public String videoName() {
        return videoName;
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

    public int widthBlocks() {
        return widthBlocks;
    }

    public int heightBlocks() {
        return heightBlocks;
    }

    public int fps() {
        return fps;
    }

    public boolean looping() {
        return looping;
    }

    public static Builder builder(String videoName, UUID worldId, String worldName, double x, double y, double z) {
        return new Builder(videoName, worldId, worldName, x, y, z);
    }

    public static final class Builder {
        private String id;
        private final String videoName;
        private final UUID worldId;
        private final String worldName;
        private final double x;
        private final double y;
        private final double z;
        private float yaw;
        private int widthBlocks = 5;
        private int heightBlocks = 3;
        private int fps = 10;
        private boolean looping = true;

        private Builder(String videoName, UUID worldId, String worldName, double x, double y, double z) {
            this.videoName = Objects.requireNonNull(videoName, "videoName");
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

        public Builder widthBlocks(int widthBlocks) {
            this.widthBlocks = widthBlocks;
            return this;
        }

        public Builder heightBlocks(int heightBlocks) {
            this.heightBlocks = heightBlocks;
            return this;
        }

        public Builder fps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder looping(boolean looping) {
            this.looping = looping;
            return this;
        }

        public VideoScreenCreateRequest build() {
            if (id == null || id.isBlank()) {
                id = "screen-" + Integer.toHexString(Objects.hash(videoName, worldName, x, y, z));
            }
            return new VideoScreenCreateRequest(this);
        }
    }
}
