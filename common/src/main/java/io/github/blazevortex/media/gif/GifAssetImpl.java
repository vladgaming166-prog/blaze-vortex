package io.github.blazevortex.media.gif;

import io.github.blazevortex.api.gif.GifAnimation;

import java.nio.file.Path;
import java.util.List;

public record GifAssetImpl(
        String name,
        Path file,
        int width,
        int height,
        int frameCount,
        List<Integer> frameDelaysMs,
        int loopCount,
        boolean hasTransparency,
        double defaultFps
) implements GifAnimation {
}
