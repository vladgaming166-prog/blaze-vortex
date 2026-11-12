package io.github.blazevortex.bukkit;

public final class ClassProbe {

    private ClassProbe() {
    }

    public static boolean present(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
