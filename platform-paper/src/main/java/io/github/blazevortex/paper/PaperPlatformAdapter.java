package io.github.blazevortex.paper;

import io.github.blazevortex.bukkit.BukkitPlatformAdapter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper 26.x adapter. World displays, maps and commands reuse the Bukkit 1.19.4+
 * implementation because those APIs remain binary-compatible. Native Adventure on
 * Paper is used through adventure-platform's Paper detection, without relocating Kyori.
 */
public final class PaperPlatformAdapter extends BukkitPlatformAdapter {

    public PaperPlatformAdapter(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "paper-26";
    }
}
