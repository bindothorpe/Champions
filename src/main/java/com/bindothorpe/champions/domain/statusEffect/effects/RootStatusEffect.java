package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class RootStatusEffect extends StatusEffect {
    private static final int UNIQUE_AMPLIFIER = 128;

    private static final Map<UUID, Long> endingTime = new HashMap<>();
    public RootStatusEffect(DomainController dc) {
        super(dc, "Root", StatusEffectType.ROOT);
    }

    @Override
    public void addEntity(UUID uuid, double duration) {
        super.addEntity(uuid, duration);
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null)
            throw new IllegalArgumentException("Entity with UUID " + uuid + " does not exist");
        if(!(entity instanceof LivingEntity))
            throw new IllegalArgumentException("Entity with UUID " + uuid + " is not a living entity");

        LivingEntity livingEntity = (LivingEntity) entity;
        PotionEffect jumpBoost = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, UNIQUE_AMPLIFIER, false, false, false);
        livingEntity.addPotionEffect(jumpBoost);
        endingTime.put(uuid, System.currentTimeMillis() + ((long) (duration * 1000)));
        dc.addStatusToEntity(uuid, new EntityStatus(EntityStatusType.MOVEMENT_SPEED, 0.0D, duration, true, true, this));

    }

    @Override
    public void removeEntity(UUID uuid) {
        super.removeEntity(uuid);
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null)
            throw new IllegalArgumentException("Entity with UUID " + uuid + " does not exist");
        if(!(entity instanceof LivingEntity))
            throw new IllegalArgumentException("Entity with UUID " + uuid + " is not a living entity");

        LivingEntity livingEntity = (LivingEntity) entity;
        List<PotionEffect> jumpBoostEffectsToKeep = new ArrayList<>();
        for(PotionEffect effect : livingEntity.getActivePotionEffects()) {
            if(effect.getType().equals(PotionEffectType.JUMP)) {
                jumpBoostEffectsToKeep.add(effect);
            }
        }

        livingEntity.removePotionEffect(PotionEffectType.JUMP);
        livingEntity.addPotionEffects(jumpBoostEffectsToKeep);
        endingTime.remove(uuid);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        for(UUID uuid : endingTime.keySet()) {
            if(endingTime.get(uuid) >= System.currentTimeMillis()) {
                removeEntity(uuid);
            }
        }
    }
}
