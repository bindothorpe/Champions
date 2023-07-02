package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public class Phage extends CustomItem {
    public Phage(CustomItemManager manager) {
        super(manager, CustomItemId.PHAGE, Set.of(CustomItemType.ATTACK), "Phage", Material.IRON_AXE, 300, List.of(CustomItemId.LONG_SWORD));
        getStatuses().add(new EntityStatus(EntityStatusType.COOLDOWN_REDUCTION, 0.15, -1, true, false, this));
    }

}
