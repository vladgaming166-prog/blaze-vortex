package io.github.blazevortex.media.audio;

import io.github.blazevortex.api.audio.SoundAsset;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public final class SoundAssetImpl implements SoundAsset {

    private final String name;
    private final Path sourceFile;
    private final String format;
    private final Duration duration;
    private volatile Path oggFile;

    public SoundAssetImpl(String name, Path sourceFile, String format, Duration duration, Path oggFile) {
        this.name = name;
        this.sourceFile = sourceFile;
        this.format = format;
        this.duration = duration;
        this.oggFile = oggFile;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Path sourceFile() {
        return sourceFile;
    }

    @Override
    public Optional<Path> oggFile() {
        return Optional.ofNullable(oggFile);
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public Duration duration() {
        return duration;
    }

    @Override
    public boolean converted() {
        return oggFile != null;
    }

    @Override
    public String namespacedKey() {
        return "blazevortex:" + name;
    }

    public void setOggFile(Path oggFile) {
        this.oggFile = oggFile;
    }
}
