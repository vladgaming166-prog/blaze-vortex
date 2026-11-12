package io.github.blazevortex.media.audio;

import io.github.blazevortex.api.audio.SoundSource;
import io.github.blazevortex.api.audio.SoundSourceCreateRequest;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class SoundSourceImpl implements SoundSource {

    private final SoundSourceCreateRequest request;
    private final AtomicBoolean playing = new AtomicBoolean();
    private final Consumer<SoundSource> playHook;
    private final Consumer<SoundSource> stopHook;

    public SoundSourceImpl(SoundSourceCreateRequest request, Consumer<SoundSource> playHook, Consumer<SoundSource> stopHook) {
        this.request = request;
        this.playHook = playHook;
        this.stopHook = stopHook;
    }

    @Override
    public String id() {
        return request.id();
    }

    @Override
    public String soundName() {
        return request.soundName();
    }

    @Override
    public UUID worldId() {
        return request.worldId();
    }

    @Override
    public String worldName() {
        return request.worldName();
    }

    @Override
    public double x() {
        return request.x();
    }

    @Override
    public double y() {
        return request.y();
    }

    @Override
    public double z() {
        return request.z();
    }

    @Override
    public float volume() {
        return request.volume();
    }

    @Override
    public float pitch() {
        return request.pitch();
    }

    @Override
    public double radius() {
        return request.radius();
    }

    @Override
    public boolean looping() {
        return request.looping();
    }

    @Override
    public boolean playing() {
        return playing.get();
    }

    @Override
    public void play() {
        playing.set(true);
        playHook.accept(this);
    }

    @Override
    public void stop() {
        playing.set(false);
        stopHook.accept(this);
    }
}
