package com.bindothorpe.champions.listeners.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.util.XpBarUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CustomDamageListener implements Listener {

    private final DomainController dc;

    public CustomDamageListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCustomDamageEvent(CustomDamageEvent event) {
        if(event.isCancelled())
            return;

        Entity damagee = event.getDamagee();
        Entity damager = event.getDamager();

        if(!(damager instanceof Player))
            return;

        Player player = (Player) damager;
        XpBarUtil.setXp(player, (int) event.getCommand().getDamage(), 1);
        player.sendMessage("Damage: " + event.getCommand().getDamage() + " Force: " + event.getCommand().getForce());

    }
}
