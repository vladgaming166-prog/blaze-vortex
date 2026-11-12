package io.github.blazevortex.emoji;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.api.emoji.Emoji;
import io.github.blazevortex.api.emoji.EmojiRegistration;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

public final class RegisteredEmoji implements Emoji {

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
    private final int sourceWidth;
    private final int sourceHeight;
    private final GlyphMetrics metrics;
    private final char glyph;
    private final char[] animationGlyphs;

    public RegisteredEmoji(
            EmojiRegistration registration,
            int sourceWidth,
            int sourceHeight,
            GlyphMetrics metrics,
            char glyph,
            char[] animationGlyphs
    ) {
        this.name = registration.name();
        this.file = registration.file();
        this.enabled = registration.enabled();
        this.size = registration.size();
        this.yOffset = registration.yOffset();
        this.animated = registration.animated();
        this.fps = registration.fps();
        this.loop = registration.loop();
        this.playbackSpeed = registration.playbackSpeed();
        this.contextSizes = new EnumMap<>(RenderContext.class);
        this.contextSizes.putAll(registration.contextSizes());
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.metrics = metrics;
        this.glyph = glyph;
        this.animationGlyphs = animationGlyphs == null ? new char[]{glyph} : animationGlyphs;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path file() {
        return file;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int yOffset() {
        return yOffset;
    }

    @Override
    public boolean animated() {
        return animated && animationGlyphs.length > 1;
    }

    @Override
    public OptionalInt fps() {
        return fps == null ? OptionalInt.empty() : OptionalInt.of(fps);
    }

    @Override
    public boolean loop() {
        return loop;
    }

    @Override
    public double playbackSpeed() {
        return playbackSpeed;
    }

    @Override
    public int sourceWidth() {
        return sourceWidth;
    }

    @Override
    public int sourceHeight() {
        return sourceHeight;
    }

    @Override
    public double aspectRatio() {
        return metrics.aspectRatio();
    }

    @Override
    public int glyphHeight() {
        return metrics.glyphHeight();
    }

    @Override
    public double glyphWidth() {
        return metrics.glyphWidth();
    }

    @Override
    public int ascent() {
        return metrics.ascent();
    }

    @Override
    public char glyph() {
        return glyph;
    }

    public char[] animationGlyphs() {
        return animationGlyphs.clone();
    }

    public char frameGlyph(long epochMs) {
        if (animationGlyphs.length <= 1) {
            return glyph;
        }
        double framesPerSecond = fps == null ? 10.0 : fps;
        framesPerSecond *= Math.max(0.1, playbackSpeed);
        int index = (int) ((epochMs / (1000.0 / framesPerSecond)) % animationGlyphs.length);
        if (index < 0) {
            index = 0;
        }
        return animationGlyphs[index];
    }

    @Override
    public String tag() {
        return "<" + name + ">";
    }

    @Override
    public OptionalInt sizeOverride(RenderContext context) {
        Integer value = contextSizes.get(context);
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    public GlyphMetrics metrics() {
        return metrics;
    }
}
