package io.github.blazevortex.media.video;

import io.github.blazevortex.api.event.BvxEventBus;
import io.github.blazevortex.api.event.VideoStartEvent;
import io.github.blazevortex.api.event.VideoStopEvent;
import io.github.blazevortex.api.video.PlaybackState;
import io.github.blazevortex.api.video.VideoScreen;
import io.github.blazevortex.api.video.VideoScreenCreateRequest;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class VideoScreenImpl implements VideoScreen {

    private final VideoScreenCreateRequest request;
    private final AtomicReference<PlaybackState> state = new AtomicReference<>(PlaybackState.STOPPED);
    private final BvxEventBus events;
    private volatile boolean looping;
    private volatile double positionSeconds;
    private volatile long playbackAnchorMs;

    public VideoScreenImpl(VideoScreenCreateRequest request, BvxEventBus events) {
        this.request = request;
        this.events = events;
        this.looping = request.looping();
    }

    @Override
    public String id() {
        return request.id();
    }

    @Override
    public String videoName() {
        return request.videoName();
    }

    @Override
    public int widthBlocks() {
        return request.widthBlocks();
    }

    @Override
    public int heightBlocks() {
        return request.heightBlocks();
    }

    @Override
    public PlaybackState state() {
        return state.get();
    }

    @Override
    public boolean looping() {
        return looping;
    }

    @Override
    public int fps() {
        return request.fps();
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

    public float yaw() {
        return request.yaw();
    }

    @Override
    public void play() {
        state.set(PlaybackState.PLAYING);
        playbackAnchorMs = System.currentTimeMillis() - (long) (positionSeconds * 1000);
        events.fire(new VideoStartEvent(this));
    }

    @Override
    public void pause() {
        if (state.get() == PlaybackState.PLAYING) {
            positionSeconds = currentPositionSeconds();
            state.set(PlaybackState.PAUSED);
        }
    }

    @Override
    public void resume() {
        if (state.get() == PlaybackState.PAUSED) {
            play();
        }
    }

    @Override
    public void stop() {
        state.set(PlaybackState.STOPPED);
        positionSeconds = 0;
        events.fire(new VideoStopEvent(this));
    }

    @Override
    public void seekSeconds(double seconds) {
        positionSeconds = Math.max(0, seconds);
        if (state.get() == PlaybackState.PLAYING) {
            playbackAnchorMs = System.currentTimeMillis() - (long) (positionSeconds * 1000);
        }
    }

    @Override
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public double currentPositionSeconds() {
        if (state.get() != PlaybackState.PLAYING) {
            return positionSeconds;
        }
        return Math.max(0, (System.currentTimeMillis() - playbackAnchorMs) / 1000.0);
    }

    public int currentFrame(int totalFrames, int fps) {
        if (totalFrames <= 0 || fps <= 0) {
            return 0;
        }
        int frame = (int) (currentPositionSeconds() * fps);
        if (looping) {
            return Math.floorMod(frame, totalFrames);
        }
        return Math.min(totalFrames - 1, Math.max(0, frame));
    }
}
