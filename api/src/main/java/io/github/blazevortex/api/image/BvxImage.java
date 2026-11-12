package io.github.blazevortex.api.image;

import java.nio.file.Path;

public interface BvxImage {

    String name();

    Path file();

    String format();

    int width();

    int height();

    double aspectRatio();

    boolean hasAlpha();

    long fileSize();
}
