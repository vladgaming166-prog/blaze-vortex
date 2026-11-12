package io.github.blazevortex.api.display;

import java.util.UUID;

public interface BvxDisplay {

    String id();

    DisplayType type();

    String assetName();

    UUID worldId();

    String worldName();

    double x();

    double y();

    double z();

    float yaw();

    float pitch();

    float width();

    float height();

    boolean visible();

    int viewDistance();

    void setVisible(boolean visible);

    void remove();
}
