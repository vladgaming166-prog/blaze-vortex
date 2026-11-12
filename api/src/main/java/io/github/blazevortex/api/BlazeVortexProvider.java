package io.github.blazevortex.api;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the running {@link BlazeVortex} instance so other plugins can obtain the API
 * without a hard compile-time dependency on implementation classes.
 */
public final class BlazeVortexProvider {

    private static final AtomicReference<BlazeVortex> INSTANCE = new AtomicReference<>();

    private BlazeVortexProvider() {
    }

    public static void register(BlazeVortex instance) {
        Objects.requireNonNull(instance, "instance");
        if (!INSTANCE.compareAndSet(null, instance)) {
            throw new IllegalStateException("Blaze Vortex is already registered");
        }
    }

    public static void unregister(BlazeVortex instance) {
        INSTANCE.compareAndSet(instance, null);
    }

    public static boolean isAvailable() {
        return INSTANCE.get() != null;
    }

    public static Optional<BlazeVortex> find() {
        return Optional.ofNullable(INSTANCE.get());
    }

    public static BlazeVortex get() {
        BlazeVortex instance = INSTANCE.get();
        if (instance == null) {
            throw new IllegalStateException("Blaze Vortex is not loaded yet");
        }
        return instance;
    }
}
