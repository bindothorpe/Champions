package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SerratedDirk extends CustomItem {

    public SerratedDirk(CustomItemManager manager) {
        super(manager, CustomItemId.SERRATED_DIRK, "Serrated Dirk", Material.IRON_SWORD, 500, List.of(CustomItemId.LONG_SWORD,CustomItemId.LONG_SWORD), List.of());
    }


}
