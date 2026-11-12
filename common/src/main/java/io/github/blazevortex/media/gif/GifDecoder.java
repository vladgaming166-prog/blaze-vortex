package io.github.blazevortex.media.gif;

import io.github.blazevortex.api.BvxException;
import io.github.blazevortex.security.MediaLimits;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Decodes animated GIFs with frame order, per-frame delay, disposal and transparency preserved.
 * Callers must invoke this off the Minecraft main thread.
 */
public final class GifDecoder {

    private final MediaLimits limits;

    public GifDecoder(MediaLimits limits) {
        this.limits = limits;
    }

    public GifSequence decode(Path file) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
            if (in == null) {
                throw new BvxException("Cannot open GIF: " + file.getFileName());
            }
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                throw new BvxException("No GIF ImageReader is available on this JVM");
            }
            ImageReader reader = readers.next();
            reader.setInput(in, false, false);
            try {
                int count = reader.getNumImages(true);
                if (count <= 0) {
                    throw new BvxException("GIF has no frames: " + file.getFileName());
                }
                if (count > limits.maxGifFrames()) {
                    throw new BvxException("GIF " + file.getFileName() + " has " + count
                            + " frames, exceeding the limit of " + limits.maxGifFrames());
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixels = (long) width * height * count;
                if (pixels > limits.maxDecodedPixels()) {
                    throw new BvxException("GIF decoded pixel count " + pixels + " exceeds limit "
                            + limits.maxDecodedPixels());
                }
                BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D canvasGraphics = canvas.createGraphics();
                List<GifFrame> frames = new ArrayList<>(count);
                BufferedImage lastDisposal = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                boolean transparent = false;
                int loopCount = loopCount(reader);
                for (int i = 0; i < count; i++) {
                    BufferedImage raw = reader.read(i);
                    IIOMetadata metadata = reader.getImageMetadata(i);
                    FrameMeta meta = FrameMeta.from(metadata);
                    if (meta.transparent) {
                        transparent = true;
                    }
                    if (meta.disposal == 3) {
                        copy(canvas, lastDisposal);
                    }
                    canvasGraphics.drawImage(raw, meta.left, meta.top, null);
                    BufferedImage snapshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    copy(canvas, snapshot);
                    frames.add(new GifFrame(snapshot, meta.delayMs, i));
                    if (meta.disposal == 2) {
                        canvasGraphics.setBackground(new Color(0, 0, 0, 0));
                        canvasGraphics.clearRect(meta.left, meta.top, raw.getWidth(), raw.getHeight());
                    } else if (meta.disposal == 3) {
                        copy(lastDisposal, canvas);
                    } else {
                        copy(canvas, lastDisposal);
                    }
                }
                canvasGraphics.dispose();
                return new GifSequence(width, height, loopCount, transparent, List.copyOf(frames));
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * Lightweight probe that does not retain decoded frame bitmaps.
     */
    public GifInfo probe(Path file) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            ImageReader reader = readers.next();
            reader.setInput(in, false, false);
            try {
                int count = Math.min(reader.getNumImages(true), limits.maxGifFrames());
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                List<Integer> delays = new ArrayList<>();
                boolean transparent = false;
                for (int i = 0; i < count; i++) {
                    FrameMeta meta = FrameMeta.from(reader.getImageMetadata(i));
                    delays.add(meta.delayMs);
                    transparent |= meta.transparent;
                }
                return new GifInfo(width, height, count, delays, loopCount(reader), transparent);
            } finally {
                reader.dispose();
            }
        }
    }

    private static int loopCount(ImageReader reader) {
        try {
            IIOMetadata metadata = reader.getStreamMetadata();
            if (metadata == null) {
                return 0;
            }
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
            IIOMetadataNode ext = child(root, "GraphicControlExtension") != null ? root : root;
            IIOMetadataNode app = child(ext, "ApplicationExtensions");
            if (app == null) {
                return 0;
            }
            for (int i = 0; i < app.getLength(); i++) {
                IIOMetadataNode node = (IIOMetadataNode) app.item(i);
                if ("NETSCAPE2.0".equals(node.getAttribute("applicationID"))
                        || "NETSCAPE2.0".equals(node.getAttribute("appID"))) {
                    byte[] bytes = (byte[]) node.getUserObject();
                    if (bytes != null && bytes.length >= 3) {
                        return (bytes[1] & 0xFF) | ((bytes[2] & 0xFF) << 8);
                    }
                }
                byte[] bytes = (byte[]) node.getUserObject();
                if (bytes != null && bytes.length >= 3 && bytes[0] == 1) {
                    return (bytes[1] & 0xFF) | ((bytes[2] & 0xFF) << 8);
                }
            }
        } catch (Exception ignored) {
            return 0;
        }
        return 0;
    }

    private static IIOMetadataNode child(IIOMetadataNode node, String name) {
        for (int i = 0; i < node.getLength(); i++) {
            if (name.equals(node.item(i).getNodeName())) {
                return (IIOMetadataNode) node.item(i);
            }
        }
        return null;
    }

    private static void copy(BufferedImage from, BufferedImage to) {
        Graphics2D g = to.createGraphics();
        g.drawImage(from, 0, 0, null);
        g.dispose();
    }

    private record FrameMeta(int delayMs, int disposal, int left, int top, boolean transparent) {
        static FrameMeta from(IIOMetadata metadata) {
            int delay = 10;
            int disposal = 0;
            int left = 0;
            int top = 0;
            boolean transparent = false;
            try {
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
                IIOMetadataNode gce = find(root, "GraphicControlExtension");
                if (gce != null) {
                    String delayAttr = gce.getAttribute("delayTime");
                    if (delayAttr != null && !delayAttr.isBlank()) {
                        delay = Math.max(10, Integer.parseInt(delayAttr) * 10);
                    }
                    String disposalAttr = gce.getAttribute("disposalMethod");
                    disposal = disposalCode(disposalAttr);
                    transparent = "TRUE".equalsIgnoreCase(gce.getAttribute("transparentColorFlag"));
                }
                IIOMetadataNode id = find(root, "ImageDescriptor");
                if (id != null) {
                    left = parseInt(id.getAttribute("imageLeftPosition"));
                    top = parseInt(id.getAttribute("imageTopPosition"));
                }
            } catch (Exception ignored) {
                // Use conservative defaults when metadata is incomplete.
            }
            return new FrameMeta(delay, disposal, left, top, transparent);
        }

        private static int disposalCode(String method) {
            if (method == null) {
                return 0;
            }
            return switch (method) {
                case "restoreToBackgroundColor" -> 2;
                case "restoreToPrevious" -> 3;
                case "doNotDispose" -> 1;
                default -> 0;
            };
        }

        private static int parseInt(String value) {
            try {
                return Integer.parseInt(value);
            } catch (Exception ex) {
                return 0;
            }
        }

        private static IIOMetadataNode find(IIOMetadataNode node, String name) {
            if (name.equals(node.getNodeName())) {
                return node;
            }
            for (int i = 0; i < node.getLength(); i++) {
                IIOMetadataNode found = find((IIOMetadataNode) node.item(i), name);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
    }
}
