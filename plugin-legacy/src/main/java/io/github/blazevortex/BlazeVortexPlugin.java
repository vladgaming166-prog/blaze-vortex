package io.github.blazevortex;

import io.github.blazevortex.api.BlazeVortexProvider;
import io.github.blazevortex.bukkit.BukkitPlatformAdapter;
import io.github.blazevortex.bukkit.command.BvxBukkitCommand;
import io.github.blazevortex.bukkit.integration.IntegrationManager;
import io.github.blazevortex.core.BlazeVortexEngine;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlazeVortexPlugin extends JavaPlugin {

    private BlazeVortexEngine engine;
    private BukkitPlatformAdapter platform;

    @Override
    public void onEnable() {
        saveResourceDefaults();
        platform = new BukkitPlatformAdapter(this);
        engine = new BlazeVortexEngine(platform);
        platform.attach(engine);
        try {
            engine.start();
        } catch (Exception ex) {
            getLogger().severe("Blaze Vortex failed to start: " + ex.getMessage());
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        BlazeVortexProvider.register(engine);
        BvxBukkitCommand command = new BvxBukkitCommand(engine);
        bind("bvx", command);
        bind("blazevortex", command);
        IntegrationManager.hook(this, engine);
    }

    @Override
    public void onDisable() {
        if (engine != null) {
            BlazeVortexProvider.unregister(engine);
            engine.shutdown();
        }
    }

    private void bind(String name, BvxBukkitCommand executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command " + name + " is missing from plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void saveResourceDefaults() {
        getDataFolder().mkdirs();
    }
}
