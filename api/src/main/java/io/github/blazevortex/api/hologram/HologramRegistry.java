package io.github.blazevortex.api.hologram;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HologramRegistry {

    Collection<Hologram> all();

    Optional<Hologram> find(String id);

    Hologram create(String id, UUID worldId, String worldName, double x, double y, double z, List<String> lines);

    boolean remove(String id);

    void reload();
}
