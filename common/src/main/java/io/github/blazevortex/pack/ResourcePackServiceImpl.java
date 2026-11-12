package io.github.blazevortex.pack;

import io.github.blazevortex.api.audio.SoundAsset;
import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.event.ResourcePackBuildEvent;
import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.api.pack.ResourcePackService;
import io.github.blazevortex.api.pack.ResourcePackStatus;
import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.PlatformAdapter;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class ResourcePackServiceImpl implements ResourcePackService {

    private final ResourcePackBuilder builder;
    private final BvxScheduler scheduler;
    private final BvxEventBus events;
    private final PlatformAdapter platform;
    private final Supplier<List<SoundAsset>> sounds;
    private final boolean autoSend;
    private final boolean required;

    public ResourcePackServiceImpl(
            ResourcePackBuilder builder,
            BvxScheduler scheduler,
            BvxEventBus events,
            PlatformAdapter platform,
            Supplier<List<SoundAsset>> sounds,
            boolean autoSend,
            boolean required
    ) {
        this.builder = builder;
        this.scheduler = scheduler;
        this.events = events;
        this.platform = platform;
        this.sounds = sounds;
        this.autoSend = autoSend;
        this.required = required;
    }

    @Override
    public CompletableFuture<ResourcePackBuildResult> build() {
        return scheduler.supplyAsync(() -> {
            ResourcePackBuildResult result = builder.build(sounds.get(), platform.minecraftVersion());
            events.fire(new ResourcePackBuildEvent(result));
            return result;
        });
    }

    @Override
    public CompletableFuture<ResourcePackBuildResult> export() {
        return build();
    }

    @Override
    public Optional<Path> lastPackFile() {
        return Optional.ofNullable(builder.lastResult()).map(ResourcePackBuildResult::packFile);
    }

    @Override
    public Optional<String> lastSha1() {
        return Optional.ofNullable(builder.lastResult()).map(ResourcePackBuildResult::sha1);
    }

    @Override
    public ResourcePackStatus status() {
        return builder.status();
    }

    public void sendToPlayers() {
        ResourcePackBuildResult result = builder.lastResult();
        if (result != null && result.success()) {
            platform.sendResourcePack(result, required);
        }
    }

    public boolean autoSend() {
        return autoSend;
    }
}
