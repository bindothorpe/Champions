package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public class Spellblade extends CustomItem {
    public Spellblade(CustomItemManager manager) {
        super(manager, CustomItemId.SPELLBLADE, Set.of(CustomItemType.ATTACK), "Spellblade", Material.GOLDEN_SWORD, 400, List.of(CustomItemId.LONG_SWORD, CustomItemId.LONG_SWORD));
    }
}
