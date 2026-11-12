package io.github.blazevortex.emoji;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.api.component.RenderContext;

/**
 * Converts an emoji's original pixel dimensions and {@code size: 1-100} into Minecraft
 * bitmap-font glyph metrics while preserving aspect ratio.
 *
 * <p>Normalization: at {@code size: 100}, {@code nativePixelReference} source pixels along
 * an axis map to {@code referenceHeight} font units. A 128x128 image and a 256x64 image
 * therefore produce different glyph widths and heights.
 */
public final class EmojiSizeEngine {

    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    private final int referenceHeight;
    private final int nativePixelReference;
    private final int maxGlyphHeight;
    private final int maxGlyphWidth;

    public EmojiSizeEngine(int referenceHeight, int nativePixelReference, int maxGlyphHeight, int maxGlyphWidth) {
        if (referenceHeight <= 0 || nativePixelReference <= 0 || maxGlyphHeight <= 0 || maxGlyphWidth <= 0) {
            throw new BvxException("Emoji size engine reference values must be positive");
        }
        this.referenceHeight = referenceHeight;
        this.nativePixelReference = nativePixelReference;
        this.maxGlyphHeight = maxGlyphHeight;
        this.maxGlyphWidth = maxGlyphWidth;
    }

    public static EmojiSizeEngine defaults() {
        return new EmojiSizeEngine(16, 128, 256, 256);
    }

    public static int validateSize(int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new BvxException("Emoji size must be an integer from " + MIN_SIZE + " to " + MAX_SIZE + ", got " + size);
        }
        return size;
    }

    public GlyphMetrics calculate(int sourceWidth, int sourceHeight, int sizePercent, int yOffset) {
        return calculate(sourceWidth, sourceHeight, sizePercent, yOffset, null);
    }

    public GlyphMetrics calculate(
            int sourceWidth,
            int sourceHeight,
            int sizePercent,
            int yOffset,
            RenderContext context
    ) {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            throw new BvxException("Cannot calculate glyph metrics for " + sourceWidth + "x" + sourceHeight);
        }
        validateSize(sizePercent);
        double aspect = (double) sourceWidth / (double) sourceHeight;
        double scale = (sizePercent / 100.0) * ((double) referenceHeight / (double) nativePixelReference);
        double glyphWidth = sourceWidth * scale;
        double glyphHeight = sourceHeight * scale;

        double widthLimit = maxGlyphWidth;
        double heightLimit = maxGlyphHeight;
        if (context != null) {
            heightLimit = Math.min(heightLimit, contextHeightLimit(context));
            widthLimit = Math.min(widthLimit, contextWidthLimit(context));
        }

        double clamp = 1.0;
        if (glyphHeight > heightLimit) {
            clamp = Math.min(clamp, heightLimit / glyphHeight);
        }
        if (glyphWidth > widthLimit) {
            clamp = Math.min(clamp, widthLimit / glyphWidth);
        }
        glyphWidth *= clamp;
        glyphHeight *= clamp;

        int height = Math.max(1, (int) Math.round(glyphHeight));
        double width = Math.max(1.0, glyphWidth);
        // Bitmap fonts render above the baseline when ascent is close to height.
        // y-offset shifts the glyph vertically without changing its size.
        int ascent = Math.max(1, height + yOffset);
        return new GlyphMetrics(sourceWidth, sourceHeight, aspect, sizePercent, width, height, ascent, yOffset, scale * clamp);
    }

    public int referenceHeight() {
        return referenceHeight;
    }

    public int nativePixelReference() {
        return nativePixelReference;
    }

    private int contextHeightLimit(RenderContext context) {
        return switch (context) {
            case SCOREBOARD, TAB_LIST -> Math.min(maxGlyphHeight, 24);
            case BOSS_BAR, ACTION_BAR -> Math.min(maxGlyphHeight, 32);
            case GUI_TITLE, GUI_ITEM, ITEM_LORE, ITEM_NAME -> Math.min(maxGlyphHeight, 48);
            case CHAT, TITLE, SUBTITLE -> Math.min(maxGlyphHeight, 64);
            default -> maxGlyphHeight;
        };
    }

    private int contextWidthLimit(RenderContext context) {
        return switch (context) {
            case SCOREBOARD -> Math.min(maxGlyphWidth, 48);
            case TAB_LIST -> Math.min(maxGlyphWidth, 64);
            default -> maxGlyphWidth;
        };
    }
}
