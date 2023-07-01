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

public class MysticalSeed extends CustomItem {
    public MysticalSeed(CustomItemManager manager) {
        super(manager, CustomItemId.MYSTICAL_SEED, Set.of(CustomItemType.DEFENSE), "Mystical Seed", Material.BEETROOT_SEEDS, 300);
        getStatuses().add(new EntityStatus(EntityStatusType.KNOCKBACK_RECEIVED, 1, -1, false, false, this));
    }
}
