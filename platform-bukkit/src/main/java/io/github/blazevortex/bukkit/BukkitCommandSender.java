package io.github.blazevortex.bukkit;

import io.github.blazevortex.command.CommandSenderView;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class BukkitCommandSender implements CommandSenderView {

    private final CommandSender sender;

    public BukkitCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String name() {
        return sender.getName();
    }

    @Override
    public UUID uuid() {
        return sender instanceof Player player ? player.getUniqueId() : new UUID(0, 0);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void send(String message) {
        sender.sendMessage(message);
    }
}
