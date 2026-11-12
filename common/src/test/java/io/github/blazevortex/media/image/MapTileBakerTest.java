package io.github.blazevortex.media.image;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class MapTileBakerTest {

    @Test
    void bakesOpaque128TilesFromArgb512Image() {
        BufferedImage source = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = source.createGraphics();
        g.setColor(new Color(220, 40, 40, 180));
        g.fillRect(0, 0, 512, 512);
        g.dispose();

        BufferedImage[][] tiles = MapTileBaker.bake(source, 2, 2);
        assertThat(tiles).hasDimensions(2, 2);
        for (BufferedImage[] row : tiles) {
            for (BufferedImage tile : row) {
                assertThat(tile.getWidth()).isEqualTo(128);
                assertThat(tile.getHeight()).isEqualTo(128);
                assertThat(tile.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
                assertThat(tile.getColorModel().hasAlpha()).isFalse();
                Color pixel = new Color(tile.getRGB(64, 64));
                assertThat(pixel.getRed()).isGreaterThan(100);
                assertThat(pixel.getGreen()).isLessThan(80);
            }
        }
    }

    @Test
    void preservesWideAspectRatioWithLetterboxing() {
        BufferedImage source = new BufferedImage(256, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = source.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 256, 64);
        g.dispose();

        BufferedImage[][] tiles = MapTileBaker.bake(source, 2, 2);
        Color topCenter = new Color(tiles[0][0].getRGB(64, 4));
        Color middle = new Color(tiles[0][0].getRGB(64, 96));
        assertThat(topCenter).isEqualTo(Color.BLACK);
        assertThat(middle.getGreen()).isGreaterThan(200);
        assertThat(middle.getRed()).isEqualTo(0);
    }
}
