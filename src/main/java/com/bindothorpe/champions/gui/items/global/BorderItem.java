package com.bindothorpe.champions.gui.items.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BorderItem extends GuiItem {
    public BorderItem() {
        super(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), event -> event.setCancelled(true));
        ItemMeta meta = getItem().getItemMeta();
        meta.displayName(Component.text(" "));
        getItem().setItemMeta(meta);
    }
}
