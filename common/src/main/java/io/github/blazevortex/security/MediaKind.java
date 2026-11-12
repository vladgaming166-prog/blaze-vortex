package io.github.blazevortex.security;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum MediaKind {
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpg", "jpeg"),
    GIF("image/gif", "gif"),
    WEBP("image/webp", "webp"),
    MP4("video/mp4", "mp4"),
    MOV("video/quicktime", "mov"),
    MP3("audio/mpeg", "mp3"),
    WAV("audio/wav", "wav"),
    OGG("audio/ogg", "ogg");

    private final String mime;
    private final Set<String> extensions;

    MediaKind(String mime, String... extensions) {
        this.mime = mime;
        this.extensions = Set.copyOf(Arrays.asList(extensions));
    }

    public String mime() {
        return mime;
    }

    public Set<String> extensions() {
        return extensions;
    }

    public boolean isImage() {
        return this == PNG || this == JPEG || this == GIF || this == WEBP;
    }

    public boolean isVideo() {
        return this == MP4 || this == MOV;
    }

    public boolean isAudio() {
        return this == MP3 || this == WAV || this == OGG;
    }

    public static Optional<MediaKind> fromExtension(String filename) {
        String ext = extension(filename);
        for (MediaKind kind : values()) {
            if (kind.extensions.contains(ext)) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }

    public static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
