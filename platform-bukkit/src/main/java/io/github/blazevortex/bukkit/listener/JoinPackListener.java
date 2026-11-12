package io.github.blazevortex.bukkit.listener;

import io.github.blazevortex.api.pack.ResourcePackBuildResult;
import io.github.blazevortex.bukkit.BukkitPlatformAdapter;
import io.github.blazevortex.core.BlazeVortexEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class JoinPackListener implements Listener {

    private final BlazeVortexEngine engine;
    private final BukkitPlatformAdapter platform;

    public JoinPackListener(BlazeVortexEngine engine, BukkitPlatformAdapter platform) {
        this.engine = engine;
        this.platform = platform;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!engine.config().packAutoSend()) {
            return;
        }
        ResourcePackBuildResult last = engine.packBuilder().lastResult();
        if (last != null && last.success()) {
            platform.sendResourcePack(last, engine.config().packRequired());
        }
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        engine.platform().logger().info(event.getPlayer().getName() + " resource pack: " + event.getStatus());
    }
}
