package io.github.blazevortex.bukkit.command;

import io.github.blazevortex.bukkit.BukkitCommandSender;
import io.github.blazevortex.bukkit.gui.BvxEditorGui;
import io.github.blazevortex.command.BvxCommandRouter;
import io.github.blazevortex.command.CommandResult;
import io.github.blazevortex.core.BlazeVortexEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class BvxBukkitCommand implements CommandExecutor, TabCompleter {

    private final BlazeVortexEngine engine;
    private final BvxCommandRouter router;

    public BvxBukkitCommand(BlazeVortexEngine engine) {
        this.engine = engine;
        this.router = new BvxCommandRouter(engine);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && "editor".equalsIgnoreCase(args[0]) && sender instanceof Player player) {
            BvxEditorGui.open(player, engine);
            return true;
        }
        CommandResult result = router.execute(new BukkitCommandSender(sender), args);
        sender.sendMessage(result.message());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return router.complete(new BukkitCommandSender(sender), args);
    }
}
