package io.github.blazevortex.bukkit.item;

import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.api.item.CustomItem;
import io.github.blazevortex.core.BlazeVortexEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class BukkitItemFactory {

    private BukkitItemFactory() {
    }

    public static ItemStack create(CustomItem item, BlazeVortexEngine engine) {
        Material material = Material.matchMaterial(item.material());
        if (material == null) {
            material = Material.PAPER;
        }
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        Component name = engine.renderer().render(item.displayName(), RenderContext.ITEM_NAME);
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
        List<String> lore = new ArrayList<>();
        for (String line : item.lore()) {
            lore.add(LegacyComponentSerializer.legacySection().serialize(
                    engine.renderer().render(line, RenderContext.ITEM_LORE)));
        }
        meta.setLore(lore);
        item.customModelData().ifPresent(meta::setCustomModelData);
        stack.setItemMeta(meta);
        return stack;
    }
}
