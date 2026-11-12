package io.github.blazevortex.api.gif;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface GifRegistry {

    Collection<GifAnimation> all();

    Optional<GifAnimation> find(String name);

    GifAnimation register(Path file);

    Path contentDirectory();
}
