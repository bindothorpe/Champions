package com.bindothorpe.champions.gui.items.customItem;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class PathItem extends GuiItem {

    public PathItem(boolean isUnlocked) {
        super(getPathItem(isUnlocked));
    }
    public PathItem() {
        super(getPathItem(false));
    }

    private static ItemStack getPathItem(boolean isUnlocked) {
        ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        if(isUnlocked) {
            item.setType(Material.YELLOW_STAINED_GLASS_PANE);
        }

        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(" "));

        item.setItemMeta(meta);
        return item;
    }
}
