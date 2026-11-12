package io.github.blazevortex.emoji;

import io.github.blazevortex.api.BvxException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class EmojiSizeEngineTest {

    private final EmojiSizeEngine engine = new EmojiSizeEngine(16, 128, 256, 256);

    @Test
    void rejectsSizeOutsideOneToOneHundred() {
        assertThatThrownBy(() -> EmojiSizeEngine.validateSize(0)).isInstanceOf(BvxException.class);
        assertThatThrownBy(() -> EmojiSizeEngine.validateSize(101)).isInstanceOf(BvxException.class);
        assertThat(EmojiSizeEngine.validateSize(1)).isEqualTo(1);
        assertThat(EmojiSizeEngine.validateSize(100)).isEqualTo(100);
    }

    @Test
    void squareImageAtFullSizeMapsReferenceHeight() {
        GlyphMetrics metrics = engine.calculate(128, 128, 100, 0);
        assertThat(metrics.glyphHeight()).isEqualTo(16);
        assertThat(metrics.glyphWidth()).isCloseTo(16.0, within(0.01));
        assertThat(metrics.aspectRatio()).isCloseTo(1.0, within(0.001));
    }

    @Test
    void wideImageKeepsAspectRatio() {
        GlyphMetrics square = engine.calculate(128, 128, 100, 0);
        GlyphMetrics wide = engine.calculate(256, 64, 100, 0);
        assertThat(wide.aspectRatio()).isCloseTo(4.0, within(0.001));
        assertThat(wide.glyphWidth()).isGreaterThan(square.glyphWidth());
        assertThat(wide.glyphHeight()).isLessThan(square.glyphHeight());
        assertThat(wide.glyphWidth() / wide.glyphHeight()).isCloseTo(4.0, within(0.05));
    }

    @Test
    void sizeFiftyIsHalfOfSizeOneHundred() {
        GlyphMetrics full = engine.calculate(128, 128, 100, 0);
        GlyphMetrics half = engine.calculate(128, 128, 50, 0);
        assertThat(half.glyphHeight()).isEqualTo(Math.max(1, full.glyphHeight() / 2));
        assertThat(half.glyphWidth()).isCloseTo(full.glyphWidth() / 2.0, within(0.6));
    }

    @Test
    void originalPixelDimensionsAffectNativeSize() {
        GlyphMetrics small = engine.calculate(64, 64, 100, 0);
        GlyphMetrics large = engine.calculate(256, 256, 100, 0);
        assertThat(large.glyphHeight()).isGreaterThan(small.glyphHeight());
        assertThat(small.aspectRatio()).isCloseTo(large.aspectRatio(), within(0.001));
    }

    @Test
    void yOffsetChangesAscentNotWidth() {
        GlyphMetrics base = engine.calculate(128, 128, 100, 0);
        GlyphMetrics shifted = engine.calculate(128, 128, 100, 4);
        assertThat(shifted.glyphHeight()).isEqualTo(base.glyphHeight());
        assertThat(shifted.ascent()).isEqualTo(base.ascent() + 4);
    }
}
