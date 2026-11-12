package io.github.blazevortex.api.item;

import java.util.Collection;
import java.util.Optional;

public interface CustomItemRegistry {

    Collection<CustomItem> all();

    Optional<CustomItem> find(String id);

    CustomItem register(CustomItem item);

    void reload();
}
