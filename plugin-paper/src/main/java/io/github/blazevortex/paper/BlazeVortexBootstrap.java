package io.github.blazevortex.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.blazevortex.BlazeVortexPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class BlazeVortexBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLogger().info("Blaze Vortex bootstrap on Paper " + context.getPluginMeta().getAPIVersion());
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new BlazeVortexPlugin();
    }
}
