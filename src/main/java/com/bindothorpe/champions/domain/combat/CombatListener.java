package com.bindothorpe.champions.domain.combat;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class CombatListener implements Listener {

    private final DomainController dc;

    public CombatListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        dc.logDamageTaken(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onDealDamage(CustomDamageEvent event) {
        if (!(event.getHitBy() instanceof Player))
            return;

        dc.logDamageDealt(event.getHitBy().getUniqueId());
    }
}
