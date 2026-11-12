package io.github.blazevortex.media.gif;

import java.util.List;

public record GifInfo(
        int width,
        int height,
        int frameCount,
        List<Integer> delaysMs,
        int loopCount,
        boolean hasTransparency
) {
    public double averageFps() {
        if (delaysMs.isEmpty()) {
            return 10;
        }
        double avg = delaysMs.stream().mapToInt(Integer::intValue).average().orElse(100);
        if (avg <= 0) {
            avg = 100;
        }
        return 1000.0 / avg;
    }
}
