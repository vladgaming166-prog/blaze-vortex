package io.github.blazevortex.api.module;

/**
 * Independently configurable engine module.
 */
public interface BvxModule {

    String id();

    String displayName();

    boolean isEnabled();

    ModuleState state();

    /**
     * Load configuration and register services. Must not touch the Bukkit world.
     */
    void load(ModuleContext context);

    /**
     * Enable runtime behaviour (listeners, tasks, world objects).
     */
    void enable();

    /**
     * Disable runtime behaviour and release resources owned by this module.
     */
    void disable();

    /**
     * Reload configuration without tearing the whole plugin down.
     */
    void reload();
}
