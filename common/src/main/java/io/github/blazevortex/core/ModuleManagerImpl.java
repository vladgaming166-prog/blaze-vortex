package io.github.blazevortex.core;

import io.github.blazevortex.api.module.BvxModule;
import io.github.blazevortex.api.module.ModuleContext;
import io.github.blazevortex.api.module.ModuleManager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class ModuleManagerImpl implements ModuleManager {

    private final Map<String, BvxModule> modules = new LinkedHashMap<>();
    private final Logger logger;

    public ModuleManagerImpl(Logger logger) {
        this.logger = logger;
    }

    public void register(BvxModule module) {
        modules.put(module.id(), module);
    }

    public void loadAll(ModuleContext context, java.util.function.Function<String, Boolean> enabled) {
        for (BvxModule module : modules.values()) {
            if (!enabled.apply(module.id())) {
                logger.info("Module " + module.id() + " is disabled in config.yml");
                continue;
            }
            module.load(context);
        }
    }

    public void enableAll(java.util.function.Function<String, Boolean> enabled) {
        for (BvxModule module : modules.values()) {
            if (enabled.apply(module.id()) && module.state() != io.github.blazevortex.api.module.ModuleState.FAILED) {
                module.enable();
            }
        }
    }

    public void disableAll() {
        List<BvxModule> reverse = new java.util.ArrayList<>(modules.values());
        java.util.Collections.reverse(reverse);
        for (BvxModule module : reverse) {
            if (module.isEnabled()) {
                try {
                    module.disable();
                } catch (Exception ex) {
                    logger.warning("Module " + module.id() + " disable failed: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public Collection<BvxModule> modules() {
        return List.copyOf(modules.values());
    }

    @Override
    public Optional<BvxModule> find(String id) {
        return Optional.ofNullable(modules.get(id));
    }

    @Override
    public boolean isEnabled(String id) {
        BvxModule module = modules.get(id);
        return module != null && module.isEnabled();
    }

    @Override
    public void reload(String id) {
        BvxModule module = modules.get(id);
        if (module == null) {
            throw new io.github.blazevortex.api.BvxException("Unknown module: " + id);
        }
        module.reload();
    }
}
