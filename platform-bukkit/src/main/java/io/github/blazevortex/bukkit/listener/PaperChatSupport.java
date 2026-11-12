package io.github.blazevortex.bukkit.listener;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.command.Permissions;
import io.github.blazevortex.core.BlazeVortexEngine;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Isolated Paper chat listener. Only referenced after AsyncChatEvent is confirmed present.
 */
public final class PaperChatSupport implements Listener {

    private final BlazeVortexEngine engine;

    private PaperChatSupport(BlazeVortexEngine engine) {
        this.engine = engine;
    }

    public static void register(BlazeVortexEngine engine, org.bukkit.plugin.Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new PaperChatSupport(engine), plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!event.getPlayer().hasPermission(Permissions.USE) && !event.getPlayer().hasPermission(Permissions.ADMIN)) {
            return;
        }
        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
        Component rendered = engine.renderer().render(plain, RenderContext.CHAT);
        event.message(rendered);
    }
}
