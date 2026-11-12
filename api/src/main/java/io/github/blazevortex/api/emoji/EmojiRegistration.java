package io.github.blazevortex.api.emoji;

import io.github.blazevortex.api.component.RenderContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class EmojiRegistration {

    private final String name;
    private final Path file;
    private final boolean enabled;
    private final int size;
    private final int yOffset;
    private final boolean animated;
    private final Integer fps;
    private final boolean loop;
    private final double playbackSpeed;
    private final Map<RenderContext, Integer> contextSizes;

    private EmojiRegistration(Builder builder) {
        this.name = builder.name;
        this.file = builder.file;
        this.enabled = builder.enabled;
        this.size = builder.size;
        this.yOffset = builder.yOffset;
        this.animated = builder.animated;
        this.fps = builder.fps;
        this.loop = builder.loop;
        this.playbackSpeed = builder.playbackSpeed;
        this.contextSizes = Collections.unmodifiableMap(new EnumMap<>(builder.contextSizes));
    }

    public String name() {
        return name;
    }

    public Path file() {
        return file;
    }

    public boolean enabled() {
        return enabled;
    }

    public int size() {
        return size;
    }

    public int yOffset() {
        return yOffset;
    }

    public boolean animated() {
        return animated;
    }

    public Integer fps() {
        return fps;
    }

    public boolean loop() {
        return loop;
    }

    public double playbackSpeed() {
        return playbackSpeed;
    }

    public Map<RenderContext, Integer> contextSizes() {
        return contextSizes;
    }

    public static Builder builder(String name, Path file) {
        return new Builder(name, file);
    }

    public static final class Builder {
        private final String name;
        private final Path file;
        private boolean enabled = true;
        private int size = 100;
        private int yOffset = 0;
        private boolean animated;
        private Integer fps;
        private boolean loop = true;
        private double playbackSpeed = 1.0;
        private final EnumMap<RenderContext, Integer> contextSizes = new EnumMap<>(RenderContext.class);

        private Builder(String name, Path file) {
            this.name = Objects.requireNonNull(name, "name");
            this.file = Objects.requireNonNull(file, "file");
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder yOffset(int yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Builder animated(boolean animated) {
            this.animated = animated;
            return this;
        }

        public Builder fps(Integer fps) {
            this.fps = fps;
            return this;
        }

        public Builder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public Builder playbackSpeed(double playbackSpeed) {
            this.playbackSpeed = playbackSpeed;
            return this;
        }

        public Builder contextSize(RenderContext context, int size) {
            this.contextSizes.put(context, size);
            return this;
        }

        public EmojiRegistration build() {
            return new EmojiRegistration(this);
        }
    }
}
