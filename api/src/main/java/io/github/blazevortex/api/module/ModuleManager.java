package io.github.blazevortex.api.module;

import java.util.Collection;
import java.util.Optional;

public interface ModuleManager {

    Collection<BvxModule> modules();

    Optional<BvxModule> find(String id);

    boolean isEnabled(String id);

    void reload(String id);
}
