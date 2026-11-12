package io.github.blazevortex.api.pack;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ResourcePackService {

    CompletableFuture<ResourcePackBuildResult> build();

    CompletableFuture<ResourcePackBuildResult> export();

    Optional<Path> lastPackFile();

    Optional<String> lastSha1();

    ResourcePackStatus status();
}
