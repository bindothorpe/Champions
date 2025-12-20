package com.bindothorpe.champions.listeners.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.XpBarUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        XpBarUtil.setXp(player, (int) event.getCommand().getDamage(), 1);

        if(event.getSource().equals(CustomDamageSource.SKILL)  || event.getSource().equals(CustomDamageSource.SKILL_PROJECTILE)) {
            ChatUtil.sendSkillHitMessage(
                    dc,
                    player,
                    damager,
                    event.getDamageSourceString(),
                    event.doSendSkillHitToCaster(),
                    event.doSendSkillHitToReceiver()
                    );
        }


        if(event.getSource().equals(CustomDamageSource.ATTACK_PROJECTILE) || event.getSource().equals(CustomDamageSource.SKILL_PROJECTILE)) {

            if(event.getCommand().shouldSuppressHitSound()) return;

            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        }
    }

    @EventHandler
    public void onTakeDamage(CustomDamageEvent event) {
        if(event.isCancelled())
            return;

        if (!(event.getDamagee() instanceof Player player))
            return;

        dc.getCombatLogger().logDamageTaken(player.getUniqueId());
        dc.getCombatLogger().logDamage(player.getUniqueId(), event.getDamager().getUniqueId(), event.getSource(), event.getDamageSourceString());
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
