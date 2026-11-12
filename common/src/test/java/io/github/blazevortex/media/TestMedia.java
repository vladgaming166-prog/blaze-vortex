package io.github.blazevortex.media;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestMedia {

    private TestMedia() {
    }

    public static Path png(Path dir, String name, int width, int height, Color color) throws IOException {
        Files.createDirectories(dir);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        Path file = dir.resolve(name);
        ImageIO.write(image, "png", file.toFile());
        return file;
    }

    public static Path gif(Path dir, String name, int width, int height, int frames, int delayCs) throws IOException {
        Files.createDirectories(dir);
        Path file = dir.resolve(name);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(file.toFile())) {
            writer.setOutput(out);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < frames; i++) {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.setColor(new Color((i * 40) % 255, 40, 200, 255));
                g.fillRect(0, 0, width, height);
                g.dispose();
                ImageWriteParam param = writer.getDefaultWriteParam();
                IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), param);
                String format = metadata.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
                IIOMetadataNode gce = new IIOMetadataNode("GraphicControlExtension");
                gce.setAttribute("disposalMethod", "none");
                gce.setAttribute("userInputFlag", "FALSE");
                gce.setAttribute("transparentColorFlag", "FALSE");
                gce.setAttribute("delayTime", String.valueOf(delayCs));
                gce.setAttribute("transparentColorIndex", "0");
                root.appendChild(gce);
                metadata.setFromTree(format, root);
                writer.writeToSequence(new IIOImage(image, null, metadata), param);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        return file;
    }
}
