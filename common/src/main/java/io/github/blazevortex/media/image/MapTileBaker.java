package io.github.blazevortex.media.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Converts arbitrary images into Minecraft map tiles.
 *
 * <p>Bukkit's {@code MapCanvas.drawImage} ignores or blanks many ARGB sources.
 * Tiles are always opaque {@link BufferedImage#TYPE_INT_RGB} 128×128 images,
 * with the original aspect ratio letterboxed onto the tile grid.
 */
public final class MapTileBaker {

    public static final int TILE_SIZE = 128;

    private MapTileBaker() {
    }

    public static BufferedImage[][] bake(BufferedImage source, int tilesX, int tilesY) {
        int columns = Math.max(1, tilesX);
        int rows = Math.max(1, tilesY);
        int canvasW = columns * TILE_SIZE;
        int canvasH = rows * TILE_SIZE;
        BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, canvasW, canvasH);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int drawW;
            int drawH;
            double scale = Math.min(canvasW / (double) Math.max(1, source.getWidth()),
                    canvasH / (double) Math.max(1, source.getHeight()));
            drawW = Math.max(1, (int) Math.round(source.getWidth() * scale));
            drawH = Math.max(1, (int) Math.round(source.getHeight() * scale));
            int x = (canvasW - drawW) / 2;
            int y = (canvasH - drawH) / 2;
            g.drawImage(source, x, y, drawW, drawH, null);
        } finally {
            g.dispose();
        }
        BufferedImage[][] tiles = new BufferedImage[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                BufferedImage tile = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
                Graphics2D tg = tile.createGraphics();
                try {
                    int sx = col * TILE_SIZE;
                    int sy = row * TILE_SIZE;
                    tg.drawImage(canvas, 0, 0, TILE_SIZE, TILE_SIZE, sx, sy, sx + TILE_SIZE, sy + TILE_SIZE, null);
                } finally {
                    tg.dispose();
                }
                tiles[row][col] = tile;
            }
        }
        return tiles;
    }
}
