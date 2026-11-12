package io.github.blazevortex.api.audio;

import java.util.UUID;

public interface SoundSource {

    String id();

    String soundName();

    UUID worldId();

    String worldName();

    double x();

    double y();

    double z();

    float volume();

    float pitch();

    double radius();

    boolean looping();

    boolean playing();

    void play();

    void stop();
}
