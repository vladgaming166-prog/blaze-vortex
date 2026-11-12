package io.github.blazevortex.media.image;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.cache.BoundedCache;
import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.security.MediaLimits;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

public final class ImageProcessor {

    private final MediaLimits limits;
    private final BoundedCache<Path, ImageMetadata> metadataCache;

    static {
        ImageIO.scanForPlugins();
    }

    public ImageProcessor(MediaLimits limits, int cacheSize) {
        this.limits = limits;
        this.metadataCache = new BoundedCache<>(cacheSize);
    }

    public ImageMetadata probe(Path file) {
        return metadataCache.getOrCompute(file.toAbsolutePath().normalize(), this::readMetadata);
    }

    public BufferedImage read(Path file) throws IOException {
        BufferedImage image = ImageIO.read(file.toFile());
        if (image == null) {
            throw new BvxException("Unable to decode image: " + file.getFileName());
        }
        validateDimensions(image.getWidth(), image.getHeight());
        return image;
    }

    public BufferedImage resize(BufferedImage source, int width, int height, boolean preserveAspect) {
        int targetW = width;
        int targetH = height;
        if (preserveAspect) {
            double aspect = (double) source.getWidth() / (double) source.getHeight();
            if (width / (double) height > aspect) {
                targetW = Math.max(1, (int) Math.round(height * aspect));
            } else {
                targetH = Math.max(1, (int) Math.round(width / aspect));
            }
        }
        validateDimensions(targetW, targetH);
        int type = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage scaled = new BufferedImage(targetW, targetH, type);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(source, 0, 0, targetW, targetH, null);
        g.dispose();
        return scaled;
    }

    public BufferedImage crop(BufferedImage source, int x, int y, int width, int height) {
        int w = Math.min(width, source.getWidth() - x);
        int h = Math.min(height, source.getHeight() - y);
        if (x < 0 || y < 0 || w <= 0 || h <= 0) {
            throw new BvxException("Invalid crop region");
        }
        return source.getSubimage(x, y, w, h);
    }

    public BufferedImage rotate(BufferedImage source, double degrees) {
        double rad = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        int w = source.getWidth();
        int h = source.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(Math.max(1, newW), Math.max(1, newH), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rotated.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate((newW - w) / 2.0, (newH - h) / 2.0);
        at.rotate(rad, w / 2.0, h / 2.0);
        g.drawImage(source, at, null);
        g.dispose();
        return rotated;
    }

    public void writePng(BufferedImage image, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        if (!ImageIO.write(image, "png", target.toFile())) {
            throw new BvxException("Failed to write PNG: " + target);
        }
    }

    public void invalidate(Path file) {
        metadataCache.invalidate(file.toAbsolutePath().normalize());
    }

    private ImageMetadata readMetadata(Path file) {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
            if (in == null) {
                throw new BvxException("Cannot open image stream: " + file.getFileName());
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new BvxException("No image reader for " + file.getFileName());
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                validateDimensions(width, height);
                boolean alpha = false;
                try {
                    alpha = reader.getRawImageType(0) != null
                            && reader.getRawImageType(0).getColorModel().hasAlpha();
                } catch (Exception ignored) {
                    // Some readers do not expose raw types; alpha is refined when the image is decoded.
                }
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                return new ImageMetadata(width, height, alpha, format, Files.size(file));
            } finally {
                reader.dispose();
            }
        } catch (IOException ex) {
            throw new BvxException("Failed to probe image " + file.getFileName() + ": " + ex.getMessage(), ex);
        }
    }

    public void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new BvxException("Image has invalid dimensions " + width + "x" + height);
        }
        if (width > limits.maxImageWidth() || height > limits.maxImageHeight()) {
            throw new BvxException("Image " + width + "x" + height + " exceeds maximum "
                    + limits.maxImageWidth() + "x" + limits.maxImageHeight());
        }
        long pixels = (long) width * (long) height;
        if (pixels > limits.maxDecodedPixels()) {
            throw new BvxException("Image pixel count " + pixels + " exceeds limit " + limits.maxDecodedPixels());
        }
    }

    public static boolean isImage(MediaKind kind) {
        return kind != null && kind.isImage();
    }
}
