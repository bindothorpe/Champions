package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public class TitansGrasp extends CustomItem {
    public TitansGrasp(CustomItemManager manager) {
        super(manager, CustomItemId.TITANS_GRASP, Set.of(CustomItemType.ATTACK), "Titan's Grasp", Material.NETHERITE_SHOVEL, 500, List.of(CustomItemId.ELDER_LEAF, CustomItemId.SPELLBLADE));
    }
}
