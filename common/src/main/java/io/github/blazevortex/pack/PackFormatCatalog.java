package io.github.blazevortex.pack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Resource-pack format numbers used by Minecraft Java Edition.
 *
 * <p>Values are taken from public pack format tables. Server-side generation is unit-tested;
 * in-game rendering of a given format is only marked tested in COMPATIBILITY.md when a
 * matching client was actually launched.
 */
public final class PackFormatCatalog {

    public record Entry(int format, String minVersion, String maxVersion) {
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry(9, "1.19", "1.19.2"),
            new Entry(12, "1.19.3", "1.19.3"),
            new Entry(13, "1.19.4", "1.19.4"),
            new Entry(15, "1.20", "1.20.1"),
            new Entry(18, "1.20.2", "1.20.2"),
            new Entry(22, "1.20.3", "1.20.4"),
            new Entry(32, "1.20.5", "1.20.6"),
            new Entry(34, "1.21", "1.21.1"),
            new Entry(42, "1.21.2", "1.21.3"),
            new Entry(46, "1.21.4", "1.21.4"),
            new Entry(55, "1.21.5", "1.21.5"),
            new Entry(63, "1.21.6", "1.21.6"),
            new Entry(64, "1.21.7", "1.21.8"),
            new Entry(69, "1.21.9", "1.21.10"),
            new Entry(75, "1.21.11", "1.21.11"),
            new Entry(84, "26.1", "26.1.2"),
            new Entry(88, "26.2", "26.2"),
            new Entry(97, "26.3", "26.3")
    );

    private PackFormatCatalog() {
    }

    public static List<Entry> all() {
        return ENTRIES;
    }

    public static int forMinecraftVersion(String version) {
        String v = version.toLowerCase(Locale.ROOT).replace("minecraft", "").trim();
        if (v.startsWith("v")) {
            v = v.substring(1);
        }
        List<Entry> matches = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (inRange(v, entry.minVersion(), entry.maxVersion())) {
                matches.add(entry);
            }
        }
        if (matches.isEmpty()) {
            return ENTRIES.get(ENTRIES.size() - 1).format();
        }
        matches.sort(Comparator.comparingInt(Entry::format));
        return matches.get(matches.size() - 1).format();
    }

    public static int minFormat() {
        return ENTRIES.get(0).format();
    }

    public static int maxFormat() {
        return ENTRIES.get(ENTRIES.size() - 1).format();
    }

    private static boolean inRange(String version, String min, String max) {
        return compare(version, min) >= 0 && compare(version, max) <= 0;
    }

    static int compare(String a, String b) {
        int[] left = parts(a);
        int[] right = parts(b);
        int len = Math.max(left.length, right.length);
        for (int i = 0; i < len; i++) {
            int lv = i < left.length ? left[i] : 0;
            int rv = i < right.length ? right[i] : 0;
            if (lv != rv) {
                return Integer.compare(lv, rv);
            }
        }
        return 0;
    }

    private static int[] parts(String version) {
        String[] bits = version.split("[^0-9]+");
        List<Integer> out = new ArrayList<>();
        for (String bit : bits) {
            if (!bit.isBlank()) {
                out.add(Integer.parseInt(bit));
            }
        }
        if (out.isEmpty()) {
            return new int[]{0};
        }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }
}
