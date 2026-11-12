package io.github.blazevortex.config;

import io.github.blazevortex.api.BvxException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads YAML without dumping the administrator's file back (which would strip comments).
 * Missing keys are filled from the bundled default at read time and optionally appended.
 */
public final class ConfigLoader {

    private final Logger logger;

    public ConfigLoader(Logger logger) {
        this.logger = logger;
    }

    public BvxConfig load(Path file, String defaultResource) {
        Map<String, Object> defaults = loadResource(defaultResource);
        Map<String, Object> user = Map.of();
        if (Files.exists(file)) {
            user = loadFile(file);
        } else {
            copyResource(defaultResource, file);
            user = loadFile(file);
        }
        List<String> missing = new ArrayList<>();
        Map<String, Object> merged = merge(defaults, user, "", missing);
        if (!missing.isEmpty()) {
            logger.warning("Configuration " + file.getFileName() + " is missing keys (defaults applied): " + missing);
            appendMissingComments(file, missing);
        }
        return new BvxConfig(merged);
    }

    public Map<String, Object> loadRaw(Path file, String defaultResource) {
        Map<String, Object> defaults = loadResource(defaultResource);
        if (!Files.exists(file)) {
            copyResource(defaultResource, file);
        }
        return merge(defaults, loadFile(file), "", new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadFile(Path file) {
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(Files.readString(file, StandardCharsets.UTF_8));
            if (loaded == null) {
                return new LinkedHashMap<>();
            }
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new BvxException(file.getFileName() + " must be a YAML mapping");
            }
            return new LinkedHashMap<>((Map<String, Object>) map);
        } catch (IOException ex) {
            throw new BvxException("Failed to read " + file + ": " + ex.getMessage(), ex);
        }
    }

    public void copyResource(String resource, Path target) {
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = resourceStream(resource)) {
                Files.copy(in, target);
            }
        } catch (IOException ex) {
            throw new BvxException("Failed to install default " + target.getFileName() + ": " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadResource(String resource) {
        try (InputStream in = resourceStream(resource)) {
            Object loaded = new Yaml().load(in);
            if (loaded instanceof Map<?, ?> map) {
                return new LinkedHashMap<>((Map<String, Object>) map);
            }
            return new LinkedHashMap<>();
        } catch (IOException ex) {
            throw new BvxException("Missing bundled config " + resource, ex);
        }
    }

    private InputStream resourceStream(String resource) {
        InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new BvxException("Bundled resource not found: " + resource);
        }
        return in;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> merge(Map<String, Object> defaults, Map<String, Object> user, String prefix, List<String> missing) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            String key = entry.getKey();
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            Object defaultValue = entry.getValue();
            if (!user.containsKey(key)) {
                missing.add(path);
                result.put(key, defaultValue);
                continue;
            }
            Object userValue = user.get(key);
            if (defaultValue instanceof Map<?, ?> defaultMap && userValue instanceof Map<?, ?> userMap) {
                result.put(key, merge((Map<String, Object>) defaultMap, (Map<String, Object>) userMap, path, missing));
            } else {
                result.put(key, userValue);
            }
        }
        for (Map.Entry<String, Object> entry : user.entrySet()) {
            result.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void appendMissingComments(Path file, List<String> missing) {
        try {
            String extra = System.lineSeparator() + "# Automatically noted missing keys (defaults in memory, file not overwritten): "
                    + String.join(", ", missing) + System.lineSeparator();
            String current = Files.readString(file, StandardCharsets.UTF_8);
            if (!current.contains("Automatically noted missing keys")) {
                Files.writeString(file, current + extra, StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            logger.warning("Could not annotate missing config keys: " + ex.getMessage());
        }
    }
}
