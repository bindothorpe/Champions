package com.bindothorpe.champions.gui.items.customItem;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ShopItem extends GuiItem {
    public ShopItem(@NotNull CustomItem item, DomainController dc, CustomItemType originType){
        super(item.getItem());
        setAction(event -> {
            dc.openShopGui(event.getWhoClicked().getUniqueId(), item.getId(), originType);
        });
    }
}
