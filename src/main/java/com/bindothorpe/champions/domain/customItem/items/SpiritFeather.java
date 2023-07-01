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

public class SpiritFeather extends CustomItem {
    public SpiritFeather(CustomItemManager manager) {
        super(manager, CustomItemId.SPIRIT_FEATHER, Set.of(CustomItemType.UTILITY), "Spirit Feather", Material.FEATHER, 1200);
        getStatuses().add(new EntityStatus(EntityStatusType.MOVEMENT_SPEED, 0.3, -1, false, false, this));
    }
}
