package io.github.blazevortex.media.gif;

import java.util.List;

public record GifSequence(
        int width,
        int height,
        int loopCount,
        boolean hasTransparency,
        List<GifFrame> frames
) {
    public int frameCount() {
        return frames.size();
    }

    public List<Integer> delaysMs() {
        return frames.stream().map(GifFrame::delayMs).toList();
    }

    public double averageFps() {
        if (frames.isEmpty()) {
            return 0;
        }
        double avgDelay = frames.stream().mapToInt(GifFrame::delayMs).average().orElse(100);
        if (avgDelay <= 0) {
            avgDelay = 100;
        }
        return 1000.0 / avgDelay;
    }
}
