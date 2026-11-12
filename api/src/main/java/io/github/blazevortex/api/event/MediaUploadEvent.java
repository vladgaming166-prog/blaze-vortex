package io.github.blazevortex.api.event;

import java.nio.file.Path;

public final class MediaUploadEvent extends BvxEvent {

    private final String category;
    private final Path file;
    private final String originalName;

    public MediaUploadEvent(String category, Path file, String originalName) {
        this.category = category;
        this.file = file;
        this.originalName = originalName;
    }

    public String category() {
        return category;
    }

    public Path file() {
        return file;
    }

    public String originalName() {
        return originalName;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }
}
