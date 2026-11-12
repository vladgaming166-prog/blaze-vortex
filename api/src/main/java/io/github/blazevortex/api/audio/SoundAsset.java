package io.github.blazevortex.api.audio;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public interface SoundAsset {

    String name();

    Path sourceFile();

    Optional<Path> oggFile();

    String format();

    Duration duration();

    boolean converted();

    String namespacedKey();
}
