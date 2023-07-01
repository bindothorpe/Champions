package com.bindothorpe.champions.gui.items.customItem;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CustomItemItem extends GuiItem {
    private final DomainController dc;

    public CustomItemItem(DomainController dc, @NotNull CustomItem customItem, UUID uuid, @NotNull CustomItemId currentGuiItemId) {
        super(customItem.getItem(uuid));
        this.dc = dc;

        setAction(event -> {
            if (event.getClick().equals(ClickType.LEFT))
                dc.getCustomItemManager().addItemToUser(event.getWhoClicked().getUniqueId(), customItem.getId());
            else if (event.getClick().equals(ClickType.RIGHT))
                dc.getCustomItemManager().removeItemFromUser(event.getWhoClicked().getUniqueId(), customItem.getId());
            dc.openShopGui(event.getWhoClicked().getUniqueId(), currentGuiItemId);
        });
    }
}
