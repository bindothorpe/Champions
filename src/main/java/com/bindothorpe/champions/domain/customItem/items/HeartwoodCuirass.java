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

public class HeartwoodCuirass extends CustomItem {
    public HeartwoodCuirass(CustomItemManager manager) {
        super(manager, CustomItemId.HEARTWOOD_CUIRASS, Set.of(CustomItemType.DEFENSE, CustomItemType.UTILITY), "Heartwood Cuirass", Material.TURTLE_HELMET, 500, List.of(CustomItemId.ELDER_LEAF, CustomItemId.SPIRIT_FEATHER));
        getStatuses().add(new EntityStatus(EntityStatusType.KNOCKBACK_RECEIVED, -1.5, -1, false, false, this));
        getStatuses().add(new EntityStatus(EntityStatusType.KNOCKBACK_RECEIVED, -0.2, -1, true, false, this));
        getStatuses().add(new EntityStatus(EntityStatusType.MOVEMENT_SPEED, 0.3, -1, false, false, this));
    }
}
