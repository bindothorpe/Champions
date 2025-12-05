package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
        LivingEntity damagee = (LivingEntity) event.getEntity();

        if ((lastHit.containsKey(damagee.getUniqueId()) && lastHit.get(damagee.getUniqueId()) + DELAY > System.currentTimeMillis())) {
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled())
            return;


        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) event.getEntity(), damager, event.getDamage(), damager.getLocation(), CustomDamageSource.ATTACK);
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

        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) event.getEntity(), damager, event.getDamage(), projectile.getLocation(), CustomDamageSource.ATTACK_PROJECTILE);
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
}
