package io.github.blazevortex.security;

import io.github.blazevortex.api.BvxException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class PathSafety {

    private PathSafety() {
    }

    public static Path resolveInside(Path root, String relative) {
        if (relative == null || relative.isBlank()) {
            throw new BvxException("Empty path");
        }
        String normalised = relative.replace('\\', '/');
        if (normalised.contains("..") || normalised.startsWith("/") || normalised.contains(":")) {
            throw new BvxException("Path traversal rejected: " + relative);
        }
        Path rootAbs = root.toAbsolutePath().normalize();
        Path resolved = rootAbs.resolve(normalised).normalize();
        if (!resolved.startsWith(rootAbs)) {
            throw new BvxException("Path escapes content root: " + relative);
        }
        return resolved;
    }

    public static String sanitizeFileName(String original) {
        if (original == null) {
            throw new BvxException("Missing file name");
        }
        String name = original.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            throw new BvxException("Invalid file name: " + original);
        }
        return name.toLowerCase(Locale.ROOT);
    }

    public static void assertRegularFile(Path file) throws IOException {
        if (!java.nio.file.Files.isRegularFile(file)) {
            throw new BvxException("Not a regular file: " + file);
        }
        if (java.nio.file.Files.isSymbolicLink(file)) {
            throw new BvxException("Symbolic links are not allowed: " + file);
        }
    }
}
