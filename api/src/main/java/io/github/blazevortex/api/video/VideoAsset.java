package io.github.blazevortex.api.video;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public interface VideoAsset {

    String name();

    Path file();

    String format();

    int width();

    int height();

    Duration duration();

    double sourceFps();

    Optional<Path> extractedAudio();

    boolean processed();
}
