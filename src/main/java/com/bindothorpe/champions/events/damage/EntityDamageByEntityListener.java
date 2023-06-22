package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityDamageByEntityListener implements Listener {

    private static final long DELAY = 500;
    private final DomainController dc;

    private final Map<UUID, Long> lastHit;

    public EntityDamageByEntityListener(DomainController dc) {
        this.dc = dc;
        this.lastHit = new HashMap<>();
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        Player damager = (Player) event.getDamager();
        LivingEntity entity = (LivingEntity) event.getEntity();

        if ((lastHit.containsKey(entity.getUniqueId()) && lastHit.get(entity.getUniqueId()) + DELAY > System.currentTimeMillis())) {
            event.setCancelled(true);
            return;
        }


        if (event.isCancelled())
            return;


        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) event.getEntity(), damager, event.getDamage(), damager.getLocation(), CustomDamageSource.ATTACK);

        if (customDamageEvent.isCancelled())
            return;


        event.setCancelled(true);

        customDamageEvent.getEntity().setHealth(Math.max(0, customDamageEvent.getEntity().getHealth() - customDamageEvent.getFinalDamage()));
        customDamageEvent.getEntity().setVelocity(customDamageEvent.getEntity().getVelocity().add(customDamageEvent.getKnockbackDirection().multiply(customDamageEvent.getFinalKnockback())));
//        TODO: Make this work
//        customDamageEvent.getEntity().playEffect(EntityEffect.HURT_EXPLOSION);
        lastHit.put(entity.getUniqueId(), System.currentTimeMillis());
    }
}
