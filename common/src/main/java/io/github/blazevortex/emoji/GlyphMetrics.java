package io.github.blazevortex.emoji;

public record GlyphMetrics(
        int sourceWidth,
        int sourceHeight,
        double aspectRatio,
        int sizePercent,
        double glyphWidth,
        int glyphHeight,
        int ascent,
        int yOffset,
        double appliedScale
) {
}
