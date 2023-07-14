package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import org.bukkit.Material;

import java.util.Set;

public class BoxingGlove extends CustomItem {
    public BoxingGlove(CustomItemManager manager) {
        super(manager, CustomItemId.BOXING_GLOVE, Set.of(CustomItemType.ATTACK), "Boxing Glove", Material.RED_WOOL, 500);
        getStatuses().add(new EntityStatus(EntityStatusType.KNOCKBACK_DONE, 1, -1, false, false, this));
    }
}
