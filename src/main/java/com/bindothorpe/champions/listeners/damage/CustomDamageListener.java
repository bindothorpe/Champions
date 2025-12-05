package com.bindothorpe.champions.listeners.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.util.XpBarUtil;
import org.bukkit.Sound;
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

        if(event.getSource().equals(CustomDamageSource.ATTACK_PROJECTILE) || event.getSource().equals(CustomDamageSource.SKILL_PROJECTILE)) {
            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        }

//        player.sendMessage("Damage: " + event.getCommand().getDamage());
    }
}
