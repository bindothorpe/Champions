package com.bindothorpe.champions.gui.items.customItem;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomItemItem extends GuiItem {
    public CustomItemItem(@NotNull CustomItem customItem) {
        super(customItem.getItem());
    }
}
