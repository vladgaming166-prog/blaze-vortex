package io.github.blazevortex.api.event;

import io.github.blazevortex.api.video.VideoScreen;

public final class VideoStopEvent extends BvxEvent {

    private final VideoScreen screen;

    public VideoStopEvent(VideoScreen screen) {
        this.screen = screen;
    }

    public VideoScreen screen() {
        return screen;
    }
}
