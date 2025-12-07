package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.combat.DamageLog;
import com.bindothorpe.champions.util.ChatUtil;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        LivingEntity damagee = (LivingEntity) event.getEntity();

        if ((lastHit.containsKey(damagee.getUniqueId()) && lastHit.get(damagee.getUniqueId()) + DELAY > System.currentTimeMillis())) {
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled())
            return;


        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) event.getEntity(), damager, event.getDamage(), damager.getLocation(), CustomDamageSource.ATTACK, null);
        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damagee, damager, event.getDamage(), damager.getLocation(), CustomDamageSource.ATTACK);

        customDamageEvent.setCommand(customDamageCommand);

        Bukkit.getPluginManager().callEvent(customDamageEvent);

        if(dc.getTeamManager().getTeamFromEntity(damager).equals(dc.getTeamManager().getTeamFromEntity(damagee))) {
            customDamageEvent.setCancelled(true);
            event.setCancelled(true);
        }

        if (customDamageEvent.isCancelled())
            return;


        event.setCancelled(true);

        customDamageCommand.execute();

        lastHit.put(damagee.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onDamageByProjectile(EntityDamageByEntityEvent event) {

        if(!(event.getDamager() instanceof Projectile))
            return;

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        Projectile projectile = (Projectile) event.getDamager();

        if(!(projectile.getShooter() instanceof Player))
            return;

        if (event.isCancelled())
            return;

        Player damager = (Player) projectile.getShooter();
        LivingEntity damagee = (LivingEntity) event.getEntity();

        //TODO: Get the skill name from the skill id from the metadata of the arrow

        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) event.getEntity(), damager, event.getDamage(), projectile.getLocation(), CustomDamageSource.ATTACK_PROJECTILE, null);
        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damagee, damager, event.getDamage(), projectile.getLocation(), CustomDamageSource.ATTACK_PROJECTILE);

        customDamageEvent.setCommand(customDamageCommand);

        Bukkit.getPluginManager().callEvent(customDamageEvent);

        if(dc.getTeamManager().getTeamFromEntity(damager).equals(dc.getTeamManager().getTeamFromEntity(damagee))) {
            customDamageEvent.setCancelled(true);
            event.setCancelled(true);
        }

        if (customDamageEvent.isCancelled())
            return;


        event.setCancelled(true);
        projectile.remove();
        customDamageCommand.execute();

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.isCancelled()) return;
        if(event instanceof EntityDamageByEntityEvent) return;
        if(!dc.getPlayerManager().hasBuildSelected(event.getEntity().getUniqueId())) return;

        dc.getCombatLogger().logDamage(
                event.getEntity().getUniqueId(),
                null,
                null,
                null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.isCancelled()) return;

        if(!dc.getPlayerManager().hasBuildSelected(event.getPlayer().getUniqueId())) return;

        event.setShouldDropExperience(false);
        event.setKeepInventory(true);
        event.getDrops().clear();

        event.deathMessage(ChatUtil.Prefix.GAME.component().append(getCustomDeathMessage(dc, dc.getCombatLogger().getLastLog(event.getPlayer().getUniqueId()))));
    }




    public static Component getCustomDeathMessage(DomainController dc, DamageLog damageLog) {

        Player player = Bukkit.getPlayer(damageLog.receiver());

        if(player == null) return null;

        if(damageLog.attacker() == null) {
            return Component.text(player.getName()).color(dc.getTeamManager().getTeamFromEntity(player).getTextColor())
                    .append(Component.text(" died.").color(NamedTextColor.GRAY));
        }

        Player attacker = Bukkit.getPlayer(damageLog.attacker());

        if(attacker == null) return null;

        Component message = Component.text(player.getName()).color(dc.getTeamManager().getTeamFromEntity(player).getTextColor())
                .append(Component.text(" was killed by ").color(NamedTextColor.GRAY))
                .append(Component.text(attacker.getName()).color(dc.getTeamManager().getTeamFromEntity(attacker).getTextColor()));



        if(damageLog.damageSourceString() == null) {
            message = message.append(Component.text(".").color(NamedTextColor.GRAY));
        } else {
            message = message.append(Component.text(" using ").color(NamedTextColor.GRAY)
                    .append(Component.text(damageLog.damageSourceString()).color(NamedTextColor.YELLOW))
                    .append(Component.text(".").color(NamedTextColor.GRAY)));
        }

        return message;
    }

}
