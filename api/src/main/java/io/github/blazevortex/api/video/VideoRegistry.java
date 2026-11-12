package io.github.blazevortex.api.video;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface VideoRegistry {

    Collection<VideoAsset> assets();

    Collection<VideoScreen> screens();

    Optional<VideoAsset> findAsset(String name);

    Optional<VideoScreen> findScreen(String id);

    CompletableFuture<VideoAsset> process(String name);

    VideoScreen createScreen(VideoScreenCreateRequest request);

    boolean removeScreen(String id);

    Path contentDirectory();
}
