package io.github.blazevortex.api.display;

import java.util.Collection;
import java.util.Optional;

public interface DisplayRegistry {

    Collection<BvxDisplay> all();

    Optional<BvxDisplay> find(String id);

    BvxDisplay create(DisplayCreateRequest request);

    boolean remove(String id);
}
