package io.github.blazevortex.core;

import io.github.blazevortex.api.BlazeVortex;
import io.github.blazevortex.api.module.ModuleContext;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class ModuleContextImpl implements ModuleContext {

    private final BlazeVortex engine;
    private final Path dataFolder;
    private final Path contentFolder;
    private final Logger logger;
    private final ServiceRegistry services;
    private final java.util.function.Function<String, Boolean> enabled;

    public ModuleContextImpl(
            BlazeVortex engine,
            Path dataFolder,
            Path contentFolder,
            Logger logger,
            ServiceRegistry services,
            java.util.function.Function<String, Boolean> enabled
    ) {
        this.engine = engine;
        this.dataFolder = dataFolder;
        this.contentFolder = contentFolder;
        this.logger = logger;
        this.services = services;
        this.enabled = enabled;
    }

    @Override
    public BlazeVortex engine() {
        return engine;
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public Path contentFolder() {
        return contentFolder;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public boolean moduleEnabled(String moduleId) {
        return enabled.apply(moduleId);
    }

    @Override
    public <T> void registerService(Class<T> type, T instance) {
        services.register(type, instance);
    }

    @Override
    public <T> T service(Class<T> type) {
        return services.require(type);
    }
}
