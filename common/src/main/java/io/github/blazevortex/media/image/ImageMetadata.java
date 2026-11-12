package io.github.blazevortex.media.image;

public record ImageMetadata(
        int width,
        int height,
        boolean hasAlpha,
        String format,
        long fileBytes
) {
    public double aspectRatio() {
        if (height <= 0) {
            return 0;
        }
        return (double) width / (double) height;
    }
}
