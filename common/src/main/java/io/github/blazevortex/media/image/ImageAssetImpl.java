package io.github.blazevortex.media.image;

import io.github.blazevortex.api.image.BvxImage;

import java.nio.file.Path;

public record ImageAssetImpl(
        String name,
        Path file,
        String format,
        int width,
        int height,
        boolean hasAlpha,
        long fileSize
) implements BvxImage {
    @Override
    public double aspectRatio() {
        return height == 0 ? 0 : (double) width / height;
    }
}
