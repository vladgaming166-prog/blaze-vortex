package io.github.blazevortex.api.audio;

import java.util.Objects;
import java.util.UUID;

public final class SoundSourceCreateRequest {

    private final String id;
    private final String soundName;
    private final UUID worldId;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float volume;
    private final float pitch;
    private final double radius;
    private final boolean looping;

    private SoundSourceCreateRequest(Builder builder) {
        this.id = builder.id;
        this.soundName = builder.soundName;
        this.worldId = builder.worldId;
        this.worldName = builder.worldName;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.volume = builder.volume;
        this.pitch = builder.pitch;
        this.radius = builder.radius;
        this.looping = builder.looping;
    }

    public String id() {
        return id;
    }

    public String soundName() {
        return soundName;
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

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public double radius() {
        return radius;
    }

    public boolean looping() {
        return looping;
    }

    public static Builder builder(String soundName, UUID worldId, String worldName, double x, double y, double z) {
        return new Builder(soundName, worldId, worldName, x, y, z);
    }

    public static final class Builder {
        private String id;
        private final String soundName;
        private final UUID worldId;
        private final String worldName;
        private final double x;
        private final double y;
        private final double z;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private double radius = 16.0;
        private boolean looping = true;

        private Builder(String soundName, UUID worldId, String worldName, double x, double y, double z) {
            this.soundName = Objects.requireNonNull(soundName, "soundName");
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

        public Builder volume(float volume) {
            this.volume = volume;
            return this;
        }

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder looping(boolean looping) {
            this.looping = looping;
            return this;
        }

        public SoundSourceCreateRequest build() {
            if (id == null || id.isBlank()) {
                id = "sound-" + Integer.toHexString(Objects.hash(soundName, worldName, x, y, z));
            }
            return new SoundSourceCreateRequest(this);
        }
    }
}
