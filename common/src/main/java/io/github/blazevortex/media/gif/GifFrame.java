package io.github.blazevortex.media.gif;

import java.awt.image.BufferedImage;

public record GifFrame(BufferedImage image, int delayMs, int index) {
}
