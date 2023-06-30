package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Phage extends CustomItem {
    public Phage(CustomItemManager manager) {
        super(manager, CustomItemId.PHAGE, "Phage", Material.IRON_AXE, 300, List.of(CustomItemId.LONG_SWORD), List.of());
    }

}
