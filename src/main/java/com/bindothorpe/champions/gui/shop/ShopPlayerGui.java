package com.bindothorpe.champions.gui.shop;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ShopPlayerGui implements Listener {

    private final DomainController dc;

    public ShopPlayerGui(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Shop"))
            return;

        if(event.getClickedInventory() == null)
            return;

        if(!event.getClickedInventory().getType().equals(InventoryType.PLAYER))
            return;

        if(event.getSlot() < 3 || event.getSlot() > 8)
            return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();

        if (item == null)
            return;

        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(!container.has(dc.getCustomItemManager().getCustomItemKey(), PersistentDataType.STRING))
            return;

        String customItemIdAsString = container.get(dc.getCustomItemManager().getCustomItemKey(), PersistentDataType.STRING);

        CustomItemId id = null;

        try {
            id = CustomItemId.valueOf(customItemIdAsString);
        } catch (IllegalArgumentException e) {
            return;
        }

        dc.getCustomItemManager().sellItem(event.getWhoClicked().getUniqueId(), id);



    }
}
