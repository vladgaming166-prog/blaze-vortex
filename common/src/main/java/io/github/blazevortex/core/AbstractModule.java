package io.github.blazevortex.core;

import io.github.blazevortex.api.module.BvxModule;
import io.github.blazevortex.api.module.ModuleContext;
import io.github.blazevortex.api.module.ModuleState;

import java.util.logging.Level;

public abstract class AbstractModule implements BvxModule {

    private final String id;
    private final String displayName;
    private volatile ModuleState state = ModuleState.UNLOADED;
    private volatile boolean enabled;
    protected ModuleContext context;

    protected AbstractModule(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public boolean isEnabled() {
        return enabled && state == ModuleState.ENABLED;
    }

    @Override
    public ModuleState state() {
        return state;
    }

    @Override
    public final void load(ModuleContext context) {
        this.context = context;
        try {
            onLoad(context);
            state = ModuleState.LOADED;
        } catch (Exception ex) {
            state = ModuleState.FAILED;
            context.logger().log(Level.SEVERE, "Module " + id + " failed to load: " + ex.getMessage(), ex);
            throw ex instanceof RuntimeException re ? re : new io.github.blazevortex.api.BvxException(ex.getMessage(), ex);
        }
    }

    @Override
    public final void enable() {
        try {
            onEnable();
            enabled = true;
            state = ModuleState.ENABLED;
        } catch (Exception ex) {
            state = ModuleState.FAILED;
            context.logger().log(Level.SEVERE, "Module " + id + " failed to enable: " + ex.getMessage(), ex);
            throw ex instanceof RuntimeException re ? re : new io.github.blazevortex.api.BvxException(ex.getMessage(), ex);
        }
    }

    @Override
    public final void disable() {
        try {
            onDisable();
        } finally {
            enabled = false;
            state = ModuleState.DISABLED;
        }
    }

    @Override
    public final void reload() {
        onReload();
    }

    protected void onLoad(ModuleContext context) {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected void onReload() {
    }
}
