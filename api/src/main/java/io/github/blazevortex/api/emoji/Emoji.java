package io.github.blazevortex.api.emoji;

import io.github.blazevortex.api.component.RenderContext;

import java.nio.file.Path;
import java.util.OptionalInt;

/**
 * A registered custom emoji backed by an image or animated GIF.
 *
 * <p>Usage in text is always {@code <name>} — never colon syntax.
 */
public interface Emoji {

    String name();

    Path file();

    boolean enabled();

    /**
     * Percentage-based rendering scale from 1 to 100 inclusive.
     */
    int size();

    int yOffset();

    boolean animated();

    OptionalInt fps();

    boolean loop();

    double playbackSpeed();

    int sourceWidth();

    int sourceHeight();

    double aspectRatio();

    /**
     * Minecraft bitmap-font height after size, aspect ratio and clamps are applied.
     */
    int glyphHeight();

    /**
     * Computed glyph width in the same font-unit space as {@link #glyphHeight()}.
     */
    double glyphWidth();

    int ascent();

    char glyph();

    String tag();

    OptionalInt sizeOverride(RenderContext context);
}
