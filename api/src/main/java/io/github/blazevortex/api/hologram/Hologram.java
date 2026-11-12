package io.github.blazevortex.api.hologram;

import java.util.List;
import java.util.UUID;

public interface Hologram {

    String id();

    UUID worldId();

    String worldName();

    double x();

    double y();

    double z();

    List<String> lines();

    void setLines(List<String> lines);

    void remove();
}
