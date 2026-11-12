package io.github.blazevortex.bukkit.gui;

import io.github.blazevortex.api.emoji.Emoji;
import io.github.blazevortex.core.BlazeVortexEngine;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class BvxEditorGui {

    private BvxEditorGui() {
    }

    public static void open(Player player, BlazeVortexEngine engine) {
        Inventory inventory = Bukkit.createInventory(player, 54, "BVX Editor");
        int slot = 0;
        for (Emoji emoji : engine.emojis().all()) {
            if (slot >= 54) {
                break;
            }
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f<" + emoji.name() + ">");
            meta.setLore(List.of(
                    "§7size: " + emoji.size(),
                    "§7source: " + emoji.sourceWidth() + "x" + emoji.sourceHeight(),
                    "§7glyph: " + emoji.glyphHeight() + " high"
            ));
            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
        player.openInventory(inventory);
        player.sendMessage(Component.text("Opened BVX editor with " + engine.emojis().all().size() + " emojis."));
    }
}
