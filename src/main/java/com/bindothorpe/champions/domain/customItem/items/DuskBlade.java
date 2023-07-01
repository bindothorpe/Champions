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

public class DuskBlade extends CustomItem {
    public DuskBlade(CustomItemManager manager) {
        super(manager, CustomItemId.DUSK_BLADE, CustomItemType.ATTACK, "Dusk Blade", Material.DIAMOND_SWORD, 500, List.of(CustomItemId.SERRATED_DIRK, CustomItemId.PHAGE), new ArrayList<>());
        getStatuses().add(new EntityStatus(EntityStatusType.MOVEMENT_SPEED, 0.2, -1, false, false, this));
        getStatuses().add(new EntityStatus(EntityStatusType.DAMAGE_DONE, 0.15, -1, true, false, this));
        getStatuses().add(new EntityStatus(EntityStatusType.DAMAGE_RECEIVED, -0.15, -1, true, false, this));
    }

}
