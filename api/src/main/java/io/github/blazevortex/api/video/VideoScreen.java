package io.github.blazevortex.api.video;

import java.util.UUID;

public interface VideoScreen {

    String id();

    String videoName();

    int widthBlocks();

    int heightBlocks();

    PlaybackState state();

    boolean looping();

    int fps();

    UUID worldId();

    String worldName();

    double x();

    double y();

    double z();

    void play();

    void pause();

    void resume();

    void stop();

    void seekSeconds(double seconds);

    void setLooping(boolean looping);
}
