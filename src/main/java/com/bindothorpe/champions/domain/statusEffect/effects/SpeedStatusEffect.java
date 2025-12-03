package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class SpeedStatusEffect extends StatusEffect {
    
    public SpeedStatusEffect(DomainController dc) {
        super(dc, "Speed", StatusEffectType.SPEED);
    }

    @Override
    public void handleEntityValueChanged(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return;

        if(isActive(uuid)) {
            System.out.println(getHighestAmplifier(uuid));
            dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(EntityStatusType.MOVEMENT_SPEED, (double) getHighestAmplifier(uuid) / 10D, -1, false, false, this));
        } else {
            dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED, this);
        }
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);
    }
}
