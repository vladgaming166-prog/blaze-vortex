package io.github.blazevortex.util;

import io.github.blazevortex.api.BvxException;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Names {

    private static final Pattern SAFE = Pattern.compile("^[a-z0-9_]{1,64}$");

    private Names() {
    }

    public static String assetName(Path file) {
        String filename = file.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String raw = dot > 0 ? filename.substring(0, dot) : filename;
        return requireSafe(raw.toLowerCase(Locale.ROOT));
    }

    public static String requireSafe(String name) {
        String normalised = name.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        if (!SAFE.matcher(normalised).matches()) {
            throw new BvxException("Invalid name '" + name + "'. Use lowercase letters, digits and underscores only.");
        }
        return normalised;
    }

    public static boolean isSafe(String name) {
        return name != null && SAFE.matcher(name.trim().toLowerCase(Locale.ROOT)).matches();
    }
}
