package io.github.blazevortex.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lightweight typed event bus used by the engine and exposed to other plugins.
 */
public final class BvxEventBus {

    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T extends BvxEvent> AutoCloseable subscribe(Class<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, key -> new ArrayList<>()).add(listener);
        return () -> {
            List<Consumer<?>> registered = listeners.get(type);
            if (registered != null) {
                registered.remove(listener);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends BvxEvent> T fire(T event) {
        List<Consumer<?>> registered = listeners.get(event.getClass());
        if (registered != null) {
            for (Consumer<?> listener : List.copyOf(registered)) {
                ((Consumer<T>) listener).accept(event);
            }
        }
        return event;
    }
}
