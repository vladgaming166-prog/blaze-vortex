package io.github.blazevortex.api.audio;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SoundRegistry {

    Collection<SoundAsset> assets();

    Collection<SoundSource> sources();

    Optional<SoundAsset> findAsset(String name);

    Optional<SoundSource> findSource(String id);

    CompletableFuture<SoundAsset> convert(String name);

    void play(SoundPlayRequest request);

    void stop(String soundName);

    void stopFor(UUID playerId, String soundName);

    SoundSource putSource(SoundSourceCreateRequest request);

    boolean removeSource(String id);

    Path contentDirectory();
}
