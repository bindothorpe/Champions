package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElderLeaf extends CustomItem {
    public ElderLeaf(CustomItemManager manager) {
        super(manager, CustomItemId.ELDER_LEAF, Set.of(CustomItemType.DEFENSE), "Elder Leaf", Material.LILY_PAD, 500, List.of(CustomItemId.MYSTICAL_SEED, CustomItemId.MYSTICAL_SEED));
        getStatuses().add(new EntityStatus(EntityStatusType.KNOCKBACK_RECEIVED, -1.5, -1, false, false, this));
    }
}
