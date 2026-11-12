package io.github.blazevortex.api.audio;

import java.util.Objects;
import java.util.UUID;

public final class SoundPlayRequest {

    private final String soundName;
    private final UUID playerId;
    private final UUID worldId;
    private final Double x;
    private final Double y;
    private final Double z;
    private final float volume;
    private final float pitch;
    private final boolean looping;
    private final boolean global;

    private SoundPlayRequest(Builder builder) {
        this.soundName = builder.soundName;
        this.playerId = builder.playerId;
        this.worldId = builder.worldId;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.volume = builder.volume;
        this.pitch = builder.pitch;
        this.looping = builder.looping;
        this.global = builder.global;
    }

    public String soundName() {
        return soundName;
    }

    public UUID playerId() {
        return playerId;
    }

    public UUID worldId() {
        return worldId;
    }

    public Double x() {
        return x;
    }

    public Double y() {
        return y;
    }

    public Double z() {
        return z;
    }

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public boolean looping() {
        return looping;
    }

    public boolean global() {
        return global;
    }

    public static Builder of(String soundName) {
        return new Builder(soundName);
    }

    public static final class Builder {
        private final String soundName;
        private UUID playerId;
        private UUID worldId;
        private Double x;
        private Double y;
        private Double z;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private boolean looping;
        private boolean global;

        private Builder(String soundName) {
            this.soundName = Objects.requireNonNull(soundName, "soundName");
        }

        public Builder player(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder location(UUID worldId, double x, double y, double z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
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

        public Builder looping(boolean looping) {
            this.looping = looping;
            return this;
        }

        public Builder global(boolean global) {
            this.global = global;
            return this;
        }

        public SoundPlayRequest build() {
            return new SoundPlayRequest(this);
        }
    }
}
