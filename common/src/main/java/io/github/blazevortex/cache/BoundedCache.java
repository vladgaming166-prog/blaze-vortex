package io.github.blazevortex.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Thread-safe LRU cache with a hard entry cap. Values are not loaded on the Minecraft
 * main thread by this class — callers must do I/O before inserting.
 */
public final class BoundedCache<K, V> {

    private final int maxEntries;
    private final Map<K, V> map;

    public BoundedCache(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > BoundedCache.this.maxEntries;
            }
        };
    }

    public synchronized Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    public synchronized V getOrCompute(K key, Function<K, V> loader) {
        V existing = map.get(key);
        if (existing != null) {
            return existing;
        }
        V loaded = loader.apply(key);
        if (loaded != null) {
            map.put(key, loaded);
        }
        return loaded;
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void invalidate(K key) {
        map.remove(key);
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized int size() {
        return map.size();
    }

    public int maxEntries() {
        return maxEntries;
    }
}
