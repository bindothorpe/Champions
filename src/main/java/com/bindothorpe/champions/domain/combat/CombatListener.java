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
        if(event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        dc.getCombatLogger().logDamageTaken(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onDealDamage(CustomDamageEvent event) {
        if(event.isCancelled())
            return;

        if (!(event.getDamager() instanceof Player))
            return;

        dc.getCombatLogger().logDamageDealt(event.getDamager().getUniqueId());
    }
}
