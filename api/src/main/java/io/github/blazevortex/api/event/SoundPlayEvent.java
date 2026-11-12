package io.github.blazevortex.api.event;

import io.github.blazevortex.api.audio.SoundPlayRequest;

public final class SoundPlayEvent extends BvxEvent {

    private final SoundPlayRequest request;

    public SoundPlayEvent(SoundPlayRequest request) {
        this.request = request;
    }

    public SoundPlayRequest request() {
        return request;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }
}
