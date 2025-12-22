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
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StasisStatusEffect extends StatusEffect {

    private static final Map<UUID, Location> locationMap = new HashMap<>();
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();

    public StasisStatusEffect(DomainController dc) {
        super(dc, "Stasis", StatusEffectType.STASIS);
    }

    @Override
    public void handleEntityValueChanged(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return;

        if(isActive(uuid)) {
            locationMap.put(uuid, entity.getLocation());
            tickCounters.put(uuid, 0);
        } else {
            locationMap.remove(uuid);
            tickCounters.remove(uuid);
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

            // Teleport entity back if moved
            if(!entity.getLocation().equals(locationMap.get(uuid))) {
                entity.teleport(locationMap.get(uuid));
            }

            // Update tick counter
            int ticks = tickCounters.getOrDefault(uuid, 0);
            tickCounters.put(uuid, ticks + 1);

            // Spawn particles
            spawnStasisParticles(entity, ticks);
        }
    }

    private void spawnStasisParticles(Entity entity, int ticks) {
        Location loc = entity.getLocation();

        if (!(entity instanceof LivingEntity living)) return;

        double height = living.getHeight();
        double radius = living.getWidth() * 1.2;

        // Ambient sparkles
        for (int i = 0; i < 3; i++) {
            double randX = (Math.random() - 0.5) * radius * 2;
            double randY = Math.random() * height;
            double randZ = (Math.random() - 0.5) * radius * 2;

            Location sparkle = loc.clone().add(randX, randY, randZ);
            loc.getWorld().spawnParticle(
                    ticks % 2 == 0 ? Particle.WAX_ON : Particle.WAX_OFF,
                    sparkle,
                    1,
                    0, 0, 0
            );
        }
    }

    @EventHandler
    public void onCustomDamageEvent(CustomDamageEvent event) {
        if(event.getDamager() != null && locationMap.containsKey(event.getDamager().getUniqueId())){
            event.setCancelled(true);
            return;
        }

        if(locationMap.containsKey(event.getDamagee().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        if (!locationMap.containsKey(event.getPlayer().getUniqueId()))
            return;
        event.setCancelled(true);
    }
}