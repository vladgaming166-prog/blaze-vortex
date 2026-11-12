package io.github.blazevortex.media.video;

import io.github.blazevortex.api.video.VideoAsset;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public final class VideoAssetImpl implements VideoAsset {

    private final String name;
    private final Path file;
    private final String format;
    private final int width;
    private final int height;
    private final Duration duration;
    private final double sourceFps;
    private volatile Path extractedAudio;
    private volatile Path frameDirectory;
    private volatile boolean processed;

    public VideoAssetImpl(String name, Path file, String format, int width, int height, Duration duration, double sourceFps) {
        this.name = name;
        this.file = file;
        this.format = format;
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.sourceFps = sourceFps;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path file() {
        return file;
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public Duration duration() {
        return duration;
    }

    @Override
    public double sourceFps() {
        return sourceFps;
    }

    @Override
    public Optional<Path> extractedAudio() {
        return Optional.ofNullable(extractedAudio);
    }

    public Optional<Path> frameDirectory() {
        return Optional.ofNullable(frameDirectory);
    }

    @Override
    public boolean processed() {
        return processed;
    }

    public void markProcessed(Path frames, Path audio) {
        this.frameDirectory = frames;
        this.extractedAudio = audio;
        this.processed = true;
    }
}
