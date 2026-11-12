package io.github.blazevortex.core;

import io.github.blazevortex.api.BvxException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceRegistry {

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T instance) {
        services.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T require(Class<T> type) {
        T value = (T) services.get(type);
        if (value == null) {
            throw new BvxException("Service not registered: " + type.getSimpleName());
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable((T) services.get(type));
    }

    public void clear() {
        services.clear();
    }
}
