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
        return fromUserInput(file.getFileName().toString());
    }

    /**
     * Accepts {@code fire}, {@code fire.png}, or {@code emojis/fire.png} and returns {@code fire}.
     */
    public static String fromUserInput(String raw) {
        String trimmed = raw.trim();
        int sep = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
        if (sep >= 0) {
            trimmed = trimmed.substring(sep + 1);
        }
        int dot = trimmed.lastIndexOf('.');
        String base = dot > 0 ? trimmed.substring(0, dot) : trimmed;
        return requireSafe(base.toLowerCase(Locale.ROOT));
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
