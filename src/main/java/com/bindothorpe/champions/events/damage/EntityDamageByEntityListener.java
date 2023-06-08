package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    private final DomainController dc;

    public EntityDamageByEntityListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player))
            return;

        Player damager = (Player) event.getDamager();



        event.setCancelled(true);
//        CustomDamageEvent customDamageEvent = new CustomDamageEvent((LivingEntity) event.getEntity(), event.getDamager(), event.getDamage(), );

    }
}
