package io.github.blazevortex.font;

import io.github.blazevortex.api.BvxException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allocates unique Unicode Private Use Area code points and persists them so a rebuild
 * never silently remaps existing emojis.
 */
public final class GlyphAllocator {

    public static final int PUA_START = 0xE000;
    public static final int PUA_END = 0xF8FF;

    private final Path registryFile;
    private final ConcurrentHashMap<String, Integer> assigned = new ConcurrentHashMap<>();
    private int next = PUA_START;

    public GlyphAllocator(Path registryFile) {
        this.registryFile = registryFile;
        load();
    }

    public char allocate(String key) {
        Integer existing = assigned.get(key);
        if (existing != null) {
            return (char) existing.intValue();
        }
        synchronized (this) {
            existing = assigned.get(key);
            if (existing != null) {
                return (char) existing.intValue();
            }
            while (assigned.containsValue(next)) {
                next++;
                if (next > PUA_END) {
                    throw new BvxException("Private Use Area exhausted; cannot allocate glyph for " + key);
                }
            }
            int codePoint = next++;
            assigned.put(key, codePoint);
            save();
            return (char) codePoint;
        }
    }

    public char require(String key) {
        Integer existing = assigned.get(key);
        if (existing == null) {
            throw new BvxException("No glyph allocated for " + key);
        }
        return (char) existing.intValue();
    }

    public boolean isAssigned(String key) {
        return assigned.containsKey(key);
    }

    public int size() {
        return assigned.size();
    }

    public Map<String, Integer> snapshot() {
        return Map.copyOf(assigned);
    }

    public synchronized void release(String key) {
        assigned.remove(key);
        save();
    }

    private void load() {
        if (!Files.isRegularFile(registryFile)) {
            return;
        }
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(Files.readString(registryFile, StandardCharsets.UTF_8));
            if (!(loaded instanceof Map<?, ?> map)) {
                return;
            }
            Object glyphs = map.get("glyphs");
            if (glyphs instanceof Map<?, ?> glyphMap) {
                int max = PUA_START - 1;
                for (Map.Entry<?, ?> entry : glyphMap.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    int value = Integer.parseInt(String.valueOf(entry.getValue()));
                    if (value < PUA_START || value > PUA_END) {
                        throw new BvxException("Glyph registry contains out-of-range code point for " + key);
                    }
                    assigned.put(key, value);
                    max = Math.max(max, value);
                }
                next = Math.max(PUA_START, max + 1);
            }
        } catch (IOException ex) {
            throw new BvxException("Failed to read glyph registry: " + ex.getMessage(), ex);
        }
    }

    private void save() {
        try {
            Files.createDirectories(registryFile.getParent());
            Map<String, Object> root = new LinkedHashMap<>();
            Map<String, Integer> glyphs = new LinkedHashMap<>();
            assigned.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> glyphs.put(entry.getKey(), entry.getValue()));
            root.put("next", next);
            root.put("glyphs", glyphs);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Files.writeString(registryFile, new Yaml(options).dump(root), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BvxException("Failed to write glyph registry: " + ex.getMessage(), ex);
        }
    }
}
