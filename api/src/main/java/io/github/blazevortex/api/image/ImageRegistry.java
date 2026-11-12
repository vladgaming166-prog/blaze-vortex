package io.github.blazevortex.api.image;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ImageRegistry {

    Collection<BvxImage> all();

    Optional<BvxImage> find(String name);

    BvxImage register(Path file);

    CompletableFuture<BvxImage> resize(String name, int width, int height);

    Path contentDirectory();
}
