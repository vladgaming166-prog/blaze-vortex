package io.github.blazevortex.media;

import io.github.blazevortex.media.gif.GifDecoder;
import io.github.blazevortex.media.gif.GifSequence;
import io.github.blazevortex.media.image.ImageMetadata;
import io.github.blazevortex.media.image.ImageProcessor;
import io.github.blazevortex.security.MediaLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ImageAndGifTest {

    @TempDir
    Path temp;

    @Test
    void probesOriginalDimensionsAndAspectRatio() throws Exception {
        Path wide = TestMedia.png(temp, "wide.png", 256, 64, new Color(0, 0, 0, 0));
        ImageProcessor processor = new ImageProcessor(MediaLimits.defaults(), 8);
        ImageMetadata meta = processor.probe(wide);
        assertThat(meta.width()).isEqualTo(256);
        assertThat(meta.height()).isEqualTo(64);
        assertThat(meta.aspectRatio()).isCloseTo(4.0, within(0.001));
        assertThat(meta.hasAlpha()).isTrue();
    }

    @Test
    void gifDecoderPreservesFrameCountAndDelay() throws Exception {
        Path gif = TestMedia.gif(temp, "spin.gif", 16, 16, 4, 10);
        GifSequence sequence = new GifDecoder(MediaLimits.defaults()).decode(gif);
        assertThat(sequence.frameCount()).isEqualTo(4);
        assertThat(sequence.width()).isEqualTo(16);
        assertThat(sequence.height()).isEqualTo(16);
        assertThat(sequence.frames()).allSatisfy(frame -> assertThat(frame.delayMs()).isGreaterThanOrEqualTo(10));
    }
}
