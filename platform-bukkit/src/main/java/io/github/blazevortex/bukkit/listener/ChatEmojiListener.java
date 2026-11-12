package io.github.blazevortex.bukkit.listener;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.bukkit.ClassProbe;
import io.github.blazevortex.command.Permissions;
import io.github.blazevortex.core.BlazeVortexEngine;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatEmojiListener implements Listener {

    private final BlazeVortexEngine engine;
    private final BukkitAudiences audiences;
    private final boolean paperChat;

    public ChatEmojiListener(BlazeVortexEngine engine, BukkitAudiences audiences, org.bukkit.plugin.Plugin plugin) {
        this.engine = engine;
        this.audiences = audiences;
        this.paperChat = ClassProbe.present("io.papermc.paper.event.player.AsyncChatEvent");
        if (paperChat) {
            PaperChatSupport.register(engine, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (paperChat) {
            return;
        }
        if (!event.getPlayer().hasPermission(Permissions.USE) && !event.getPlayer().hasPermission(Permissions.ADMIN)) {
            return;
        }
        String rendered = engine.renderer().renderLegacy(event.getMessage(), RenderContext.CHAT);
        event.setMessage(rendered);
    }
}
