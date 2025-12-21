package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StunStatusEffect extends StatusEffect {

    private static final Map<UUID, Location> locationMap = new HashMap<>();

    public StunStatusEffect(DomainController dc) {
        super(dc, "Stun", StatusEffectType.STUN);
    }


    @Override
    public void handleEntityValueChanged(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return;

        if(isActive(uuid)) {
            locationMap.put(uuid, entity.getLocation());
        } else {
            locationMap.remove(uuid);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for (UUID uuid : locationMap.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null)
                continue;

            if(entity.getLocation().equals(locationMap.get(uuid)))
                continue;

            entity.teleport(locationMap.get(uuid));
        }
    }

    @EventHandler
    public void onCustomDamageEvent(CustomDamageEvent event) {
        if(event.getDamager() == null) return;
        if (!locationMap.containsKey(event.getDamager().getUniqueId()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        if (!locationMap.containsKey(event.getPlayer().getUniqueId()))
            return;
        event.setCancelled(true);
    }
}
