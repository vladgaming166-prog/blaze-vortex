package io.github.blazevortex.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Identifies media by magic bytes. File extensions are never trusted on their own.
 */
public final class MagicBytes {

    private static final byte[] PNG = hex("89504E470D0A1A0A");
    private static final byte[] JPEG = hex("FFD8FF");
    private static final byte[] GIF87 = "GIF87a".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] GIF89 = "GIF89a".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] RIFF = "RIFF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] WAVE = "WAVE".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] OGG = "OggS".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] ID3 = "ID3".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final byte[] FTYP = "ftyp".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

    private MagicBytes() {
    }

    public static Optional<MediaKind> detect(Path file) throws IOException {
        byte[] header = readHeader(file, 32);
        return detect(header);
    }

    public static Optional<MediaKind> detect(byte[] header) {
        if (startsWith(header, PNG)) {
            return Optional.of(MediaKind.PNG);
        }
        if (startsWith(header, JPEG)) {
            return Optional.of(MediaKind.JPEG);
        }
        if (startsWith(header, GIF87) || startsWith(header, GIF89)) {
            return Optional.of(MediaKind.GIF);
        }
        if (startsWith(header, OGG)) {
            return Optional.of(MediaKind.OGG);
        }
        if (startsWith(header, ID3) || isMpegFrame(header)) {
            return Optional.of(MediaKind.MP3);
        }
        if (startsWith(header, RIFF) && header.length >= 12) {
            byte[] fourcc = Arrays.copyOfRange(header, 8, 12);
            if (Arrays.equals(fourcc, WEBP)) {
                return Optional.of(MediaKind.WEBP);
            }
            if (Arrays.equals(fourcc, WAVE)) {
                return Optional.of(MediaKind.WAV);
            }
        }
        if (containsAt(header, FTYP, 4)) {
            String brand = brand(header);
            if (brand.contains("qt") || brand.contains("m4v")) {
                return Optional.of(MediaKind.MOV);
            }
            return Optional.of(MediaKind.MP4);
        }
        return Optional.empty();
    }

    public static MediaKind requireMatching(Path file, MediaKind declared) throws IOException {
        MediaKind detected = detect(file).orElseThrow(() ->
                new SecurityException("Unrecognised or unsupported file header: " + file.getFileName()));
        if (declared == MediaKind.JPEG && detected == MediaKind.JPEG) {
            return detected;
        }
        if (declared != detected) {
            throw new SecurityException("File extension claims " + declared + " but magic bytes identify " + detected
                    + " (" + file.getFileName() + ")");
        }
        return detected;
    }

    private static boolean isMpegFrame(byte[] header) {
        return header.length >= 2 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xE0) == 0xE0;
    }

    private static String brand(byte[] header) {
        if (header.length < 12) {
            return "";
        }
        return new String(header, 8, Math.min(4, header.length - 8), java.nio.charset.StandardCharsets.US_ASCII)
                .toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsAt(byte[] data, byte[] needle, int offset) {
        if (data.length < offset + needle.length) {
            return false;
        }
        for (int i = 0; i < needle.length; i++) {
            if (data[offset + i] != needle[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] readHeader(Path file, int length) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[length];
            int read = in.read(buffer);
            if (read < 0) {
                return new byte[0];
            }
            return Arrays.copyOf(buffer, read);
        }
    }

    private static byte[] hex(String value) {
        int len = value.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
                    + Character.digit(value.charAt(i + 1), 16));
        }
        return data;
    }
}
