package io.github.blazevortex.bukkit.integration;

import io.github.blazevortex.core.BlazeVortexEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class IntegrationManager {

    private IntegrationManager() {
    }

    public static void hook(Plugin plugin, BlazeVortexEngine engine) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new PlaceholderApiHook(plugin, engine).register();
                plugin.getLogger().info("Hooked PlaceholderAPI (%bvx_emoji_<name>%).");
            } catch (Throwable ex) {
                plugin.getLogger().warning("PlaceholderAPI was present but could not be hooked: " + ex.getMessage());
            }
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            plugin.getLogger().info("Vault detected. Economy is not required for BVX and remains optional.");
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            plugin.getLogger().info("WorldGuard detected. Region sound triggers can be added via configuration without a hard dependency.");
        }
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            plugin.getLogger().info("ProtocolLib is installed but BVX does not require it for core features.");
        }
    }
}
