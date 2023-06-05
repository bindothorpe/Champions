package com.bindothorpe.champions.gui.items;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BorderItem extends GuiItem {
    public BorderItem() {
        super(new ItemStack(Material.GLASS_PANE), event -> event.setCancelled(true));
    }
}
