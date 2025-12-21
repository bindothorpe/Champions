package com.bindothorpe.champions.listeners.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.util.ChatUtil;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustomDamageEvent(CustomDamageEvent event) {
        if(event.isCancelled())
            return;

        Entity damagee = event.getDamagee();
        Entity damager = event.getDamager();

        if(!(damager instanceof Player))
            return;

        Player player = (Player) damager;
        //TODO: Find a solution to get the final calculated damage
        // Maybe one of the options is, for the damage received and damage dealt modifiers, I use a listener instead of calculating it with a function?
        XpBarUtil.setXp(player, (int) event.getDamage(), 1);

        handleSkillHitMessage(event);


        if(event.getCause().equals(CustomDamageEvent.DamageCause.ATTACK_PROJECTILE) || event.getCause().equals(CustomDamageEvent.DamageCause.SKILL_PROJECTILE)) {

//            if(event.getCommand().shouldSuppressHitSound()) return;

            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        }
    }

    private void handleSkillHitMessage(CustomDamageEvent event) {

        if(event.getDamager() == null) return;

        if(event.sendSkillHitToCaster() && event.getCauseDisplayName() != null && event.getDamager() instanceof Player damager) {
            ChatUtil.sendSkillHitMessageToCaster(dc, damager, event.getDamagee(), event.getCauseDisplayName());
        }

        if(event.sendSkillHitToReceiver() && event.getCauseDisplayName() != null && event.getDamagee() instanceof Player damagee) {
            ChatUtil.sendSkillHitMessageToReceiver(dc, damagee, event.getDamager(), event.getCauseDisplayName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTakeDamage(CustomDamageEvent event) {
        if(event.isCancelled())
            return;


        if (event.getDamagee() instanceof Player damagee) {
            dc.getCombatLogger().logDamageTaken(damagee.getUniqueId());
            dc.getCombatLogger().logDamage(damagee.getUniqueId(), event.getDamager() == null ? null : event.getDamager().getUniqueId(), event.getCause(), event.getCauseDisplayName());
        }

        if(event.getDamager() instanceof Player damager) {
            dc.getCombatLogger().logDamageDealt(damager.getUniqueId());
        }


    }

    @EventHandler
    public void onDealDamage(CustomDamageEvent event) {
        if(event.isCancelled())
            return;

        if (!(event.getDamager() instanceof Player player))
            return;

        dc.getCombatLogger().logDamageDealt(player.getUniqueId());
    }
}
