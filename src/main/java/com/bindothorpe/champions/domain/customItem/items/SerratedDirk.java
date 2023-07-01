package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public class SerratedDirk extends CustomItem {

    public SerratedDirk(CustomItemManager manager) {
        super(manager, CustomItemId.SERRATED_DIRK, Set.of(CustomItemType.ATTACK), "Serrated Dirk", Material.IRON_SWORD, 500, List.of(CustomItemId.LONG_SWORD,CustomItemId.LONG_SWORD));
    }


}
