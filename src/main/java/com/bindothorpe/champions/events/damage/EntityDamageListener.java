package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.command.death.CustomDeathCommand;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.death.CustomDeathEvent;
import com.bindothorpe.champions.util.ItemUtil;
import com.bindothorpe.champions.util.PersistenceUtil;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityDamageListener implements Listener {

    private static final long DELAY = 500;

    private final DomainController dc;
    private final Map<UUID, Long> lastHit;

    public EntityDamageListener(DomainController dc) {
        this.dc = dc;
        this.lastHit = new HashMap<>();
        dc.getPlugin().getLogger().info("EntityDamageListener registered");
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity damager))
            return;

        if (!(event.getEntity() instanceof LivingEntity damagee))
            return;

        if ((lastHit.containsKey(damagee.getUniqueId()) && lastHit.get(damagee.getUniqueId()) + DELAY > System.currentTimeMillis())) {
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled())
            return;


        double damage = damager.getEquipment() == null ? 1 : getDamageFromItemInHand(damager.getEquipment().getItemInMainHand());

        CustomDamageEvent customDamageEvent = CustomDamageEvent
                .getBuilder()
                .setDamage(damage)
                .setCause(CustomDamageEvent.DamageCause.ATTACK)
                .setDamagee(damagee)
                .setDamager(damager)
                .setLocation(damager.getLocation())
                .build();

        if(!dc.getTeamManager().areEntitiesOnDifferentTeams(damager, damagee)) {
            customDamageEvent.setCancelled(true);
            event.setCancelled(true);
        }

        customDamageEvent.callEvent();


        if (customDamageEvent.isCancelled()) {
            return;
        }


        event.setCancelled(true);

        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, customDamageEvent);
        customDamageCommand.execute();

        lastHit.put(damagee.getUniqueId(), System.currentTimeMillis());
    }

    private double getDamageFromItemInHand(@Nullable ItemStack itemInMainHand) {
        if(itemInMainHand == null) return 1.0;

        Material material = itemInMainHand.getType();

        if(ItemUtil.isWeapon(itemInMainHand)) {
            if(ItemUtil.isIron(material) || ItemUtil.isGolden(material)) return 6.0;
            if(ItemUtil.isDiamond(material)) return 7.0;
        }

        return 1.0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityLaunchProjectile(ProjectileLaunchEvent event) {
        if(event.isCancelled()) return;
        PersistenceUtil.setDamageCauseForProjectile(dc, event.getEntity(), CustomDamageEvent.DamageCause.ATTACK_PROJECTILE, false);
    }

    @EventHandler
    public void onDamageByProjectile(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;

        if(!(event.getDamager() instanceof Projectile projectile))
            return;

        if (!(event.getEntity() instanceof LivingEntity damagee))
            return;

        if(!(projectile.getShooter() instanceof Player damager))
            return;


        CustomDamageEventBuilder customDamageEventBuilder = CustomDamageEvent
                .getBuilder()
                .setDamage(event.getDamage())
                .setDamager(damager)
                .setDamagee(damagee)
                .setProjectile(projectile)
                .setLocation(projectile.getLocation())
                .setCause(PersistenceUtil.getDamageCauseOfProjectile(dc, projectile));


        SkillId projectileSkillId = PersistenceUtil.getSkillIdOfProjectile(dc, projectile);
        if(projectileSkillId != null) {
            customDamageEventBuilder.setCauseDisplayName(dc.getSkillManager().getSkillName(projectileSkillId));
        }

        CustomDamageEvent customDamageEvent = customDamageEventBuilder.build();

        if(dc.getTeamManager().getTeamFromEntity(damager).equals(dc.getTeamManager().getTeamFromEntity(damagee))) {
            customDamageEvent.setCancelled(true);
            event.setCancelled(true);
        }

        customDamageEvent.callEvent();

        if (customDamageEvent.isCancelled())
            return;


        event.setCancelled(true);
        projectile.remove();
        new CustomDamageCommand(dc, customDamageEvent).execute();

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.isCancelled()) return;
        if(event instanceof EntityDamageByEntityEvent) return;
        if(!dc.getPlayerManager().hasBuildSelected(event.getEntity().getUniqueId())) return;

        if(!(event.getEntity() instanceof LivingEntity damagee)) return;

        System.out.println(event.getDamage());
        CustomDamageEvent customDamageEvent = CustomDamageEvent
                .getBuilder()
                .setDamage(event.getDamage())
                .setDamagee(damagee)
                .setCause(CustomDamageEvent.DamageCause.getValueOf(event.getCause()))
                .build();

        customDamageEvent.callEvent();

        if (customDamageEvent.isCancelled())
            return;

        event.setCancelled(true);
        new CustomDamageCommand(dc, customDamageEvent).execute();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.isCancelled()) return;

        if(!dc.getPlayerManager().hasBuildSelected(event.getPlayer().getUniqueId())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        CustomDeathEvent customDeathEvent = new CustomDeathEvent(player);
        customDeathEvent.setDeathMessage(CustomDamageCommand.getCustomDeathMessage(dc, dc.getCombatLogger().getLastLog(player.getUniqueId())));
        if(player.getRespawnLocation() == null) customDeathEvent.setRespawnLocation(player.getWorld().getSpawnLocation());
        customDeathEvent.callEvent();
        System.out.println("Custom death event is called from EntityDamageByEntityListener#onPlayerDeath");

        if(customDeathEvent.isCancelled()) return;

        CustomDeathCommand customDeathCommand = new CustomDeathCommand(customDeathEvent);

        customDeathCommand.execute();
    }

}
