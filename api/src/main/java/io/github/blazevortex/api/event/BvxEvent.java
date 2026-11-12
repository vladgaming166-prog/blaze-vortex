package io.github.blazevortex.api.event;

/**
 * Base type for BVX events. Platform adapters also fire equivalent Bukkit events
 * so listeners written against {@code org.bukkit.event} continue to work.
 */
public abstract class BvxEvent {

    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (!isCancellable()) {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " cannot be cancelled");
        }
        this.cancelled = cancelled;
    }

    public boolean isCancellable() {
        return false;
    }
}
