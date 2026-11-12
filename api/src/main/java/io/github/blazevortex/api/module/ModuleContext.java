package io.github.blazevortex.api.module;

import io.github.blazevortex.api.BlazeVortex;

import java.nio.file.Path;
import java.util.logging.Logger;

public interface ModuleContext {

    BlazeVortex engine();

    Path dataFolder();

    Path contentFolder();

    Logger logger();

    boolean moduleEnabled(String moduleId);

    <T> void registerService(Class<T> type, T instance);

    <T> T service(Class<T> type);
}
