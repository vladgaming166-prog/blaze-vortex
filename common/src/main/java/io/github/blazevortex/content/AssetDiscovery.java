package io.github.blazevortex.content;

import io.github.blazevortex.security.MediaKind;
import io.github.blazevortex.security.PathSafety;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class AssetDiscovery {

    private AssetDiscovery() {
    }

    public static List<Path> discover(Path root, Predicate<Path> filter) throws IOException {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .filter(filter)
                    .forEach(path -> {
                        PathSafety.resolveInside(root, root.relativize(path).toString());
                        files.add(path);
                    });
        }
        files.sort(Path::compareTo);
        return files;
    }

    public static Predicate<Path> extension(String... extensions) {
        List<String> allowed = new ArrayList<>();
        for (String extension : extensions) {
            allowed.add(extension.toLowerCase(Locale.ROOT));
        }
        return path -> {
            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            int dot = name.lastIndexOf('.');
            if (dot < 0) {
                return false;
            }
            return allowed.contains(name.substring(dot + 1));
        };
    }

    public static Predicate<Path> kinds(MediaKind... kinds) {
        List<String> allowed = new ArrayList<>();
        for (MediaKind kind : kinds) {
            allowed.addAll(kind.extensions());
        }
        return path -> {
            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            int dot = name.lastIndexOf('.');
            return dot >= 0 && allowed.contains(name.substring(dot + 1));
        };
    }
}
