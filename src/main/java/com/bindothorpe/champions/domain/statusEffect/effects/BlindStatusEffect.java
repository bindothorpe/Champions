package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class BlindStatusEffect extends StatusEffect {
    public BlindStatusEffect(DomainController dc) {
        super(dc, "Blind", StatusEffectType.BLIND);
    }

    @Override
    public void handleEntityValueChanged(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if(!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        livingEntity.removePotionEffect(PotionEffectType.BLINDNESS);
        if(isActive(uuid)) {
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, -1, getHighestAmplifier(uuid)));
        }

    }
}
