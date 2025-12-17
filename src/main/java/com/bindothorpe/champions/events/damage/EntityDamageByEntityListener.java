package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.command.death.CustomDeathCommand;
import com.bindothorpe.champions.domain.combat.DamageLog;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.death.CustomDeathEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ItemUtil;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityDamageByEntityListener implements Listener {

    private static final long DELAY = 500;
    private final DomainController dc;

    private final Map<UUID, Long> lastHit;

    private final Map<UUID, Long> lastMessageMap = new HashMap<>();

    public EntityDamageByEntityListener(DomainController dc) {
        this.dc = dc;
        this.lastHit = new HashMap<>();
        dc.getPlugin().getLogger().info("EntityDamageByEntityListener registered");
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

        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, damagee, damager, damage, damager.getLocation(), CustomDamageSource.ATTACK, null);
        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damagee, damager, damage, damager.getLocation(), CustomDamageSource.ATTACK);

        customDamageEvent.setCommand(customDamageCommand);

        Bukkit.getPluginManager().callEvent(customDamageEvent);

        if(!dc.getTeamManager().areEntitiesOnDifferentTeams(damager, damagee)) {
            customDamageEvent.setCancelled(true);
            event.setCancelled(true);
        }

        if (customDamageEvent.isCancelled()) {
            System.out.println("custom damage event was cancelled.");
            return;
        }

        System.out.println("Custom damage event is succesfully passed");


        event.setCancelled(true);

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
        CustomDamageEvent.addCustomDamageSourceData(dc, event.getEntity(), CustomDamageSource.ATTACK_PROJECTILE, false);
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
//        SkillId skillId = CustomDamageEvent.getSkillIdData(dc, projectile);
        CustomDamageSource customDamageSource = CustomDamageEvent.getCustomDamageSourceData(dc, projectile);

        if(customDamageSource == null) {
            event.setCancelled(true);
            return;
        }



        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) event.getEntity(), damager, projectile, event.getDamage(), projectile.getLocation(), customDamageSource, null);
        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damagee, damager, event.getDamage(), projectile.getLocation(), customDamageSource);

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
