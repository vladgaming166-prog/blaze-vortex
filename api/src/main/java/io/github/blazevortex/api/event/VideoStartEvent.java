package io.github.blazevortex.api.event;

import io.github.blazevortex.api.video.VideoScreen;

public final class VideoStartEvent extends BvxEvent {

    private final VideoScreen screen;

    public VideoStartEvent(VideoScreen screen) {
        this.screen = screen;
    }

    public VideoScreen screen() {
        return screen;
    }
}
