package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.EntityDamageListener;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ItemUtil;
import com.bindothorpe.champions.util.MobilityUtil;
import com.bindothorpe.champions.util.actionBar.ActionBarPriority;
import com.bindothorpe.champions.util.actionBar.ActionBarUtil;
import com.bindothorpe.champions.util.raycast.RaycastResult;
import com.bindothorpe.champions.util.raycast.RaycastUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PhaseWarp extends Skill implements ReloadableData {

    private final Map<UUID, Timer> activeMap = new HashMap<>();

    private double BASE_WARP_DISTANCE;
    private double WARP_DISTANCE_INCREASE_PER_LEVEL;
    private double BASE_DAMAGE;
    private double DAMAGE_INCREASE_PER_LEVEL;
    private double DASH_STRENGTH;
    private double RECAST_DURATION;

    public PhaseWarp(DomainController dc) {
        super(dc, "Phase Warp", SkillId.PHASE_WARP, SkillType.SWORD, ClassType.MAGE);
    }

    @EventHandler
    public void onLeftClick(PlayerLeftClickEvent event) {
        if(event.getPlayer() == null) return;
        if(!activeMap.containsKey(event.getPlayer().getUniqueId())) return;

        RaycastResult result = RaycastUtil.drawRaycastFromPlayerInLookingDirection(
                event.getPlayer(),
                calculateBasedOnLevel(BASE_WARP_DISTANCE, WARP_DISTANCE_INCREASE_PER_LEVEL, getSkillLevel(event.getPlayer())),
                0.5,
                0.25,
                false,
                false,
                false,
                livingEntity -> dc.getTeamManager().areEntitiesOnDifferentTeams(event.getPlayer(), livingEntity));

        if(result.getFirstHit() == null) return;
        performWarp(event.getPlayer(), result.getFirstHit(), result);
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!event.getCause().equals(CustomDamageEvent.DamageCause.ATTACK)) return;

        if(!(event.getDamager() instanceof Player damager)) return;

        if(!activeMap.containsKey(damager.getUniqueId())) return;

        if(event.getCauseDisplayName() != null && event.getCauseDisplayName().equals(getName())) return;

        event.modifyDamage(calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(damager)));

        Timer timer = activeMap.get(damager.getUniqueId());
        if(timer != null) timer.stopAndExecute();
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        performDash(event.getPlayer());
    }

    private void performDash(@NotNull Player player) {
        MobilityUtil.launch(player,
                player.getLocation().getDirection(),
                DASH_STRENGTH,
                false,
                0.0D,
                0.0D,
                0.0D,
                true
        );
        Timer timer = new Timer(dc.getPlugin(), RECAST_DURATION, () ->  {
            activeMap.remove(player.getUniqueId());
            if(ItemUtil.isSword(player.getInventory().getItemInMainHand().getType())) clearActionBar(player);
        });

        timer.start();
        activeMap.put(player.getUniqueId(), timer);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_PHASE_WARP_DASH);
    }

    private void performWarp(@NotNull Player player, @NotNull LivingEntity target, @NotNull RaycastResult result) {
        Location startLocation = player.getLocation().clone();

        Vector direction = MobilityUtil.directionTo(target.getLocation(), player.getLocation());
        Location location = target.getLocation().clone().add(direction.multiply(2));
        location.setDirection(player.getLocation().getDirection());

        player.teleport(location);

        spawnWarpParticles(startLocation, location, result.raycastPoints());

        createAndCallCustomDamageEvent(player, target);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_PHASE_WARP_WARP);

        Timer timer = activeMap.get(player.getUniqueId());
        if(timer != null) timer.stopAndExecute();
    }

    private void spawnWarpParticles(@NotNull Location start, @NotNull Location end, @NotNull List<Vector> path) {
        // Starting location - dissipating effect
        start.getWorld().spawnParticle(
                Particle.GLOW_SQUID_INK,
                start.clone().add(0, 1, 0),
                25,
                0.4, 0.6, 0.4,
                0.05
        );

        // Trail along the path - faint
        for(int i = 0; i < path.size(); i += 3) {
            Vector point = path.get(i);
            Location particleLoc = new Location(start.getWorld(), point.getX(), point.getY(), point.getZ());
            particleLoc.getWorld().spawnParticle(
                    Particle.GLOW_SQUID_INK,
                    particleLoc,
                    2,
                    0.1, 0.1, 0.1,
                    0.0
            );
        }

        // Ending location - materializing burst
        end.getWorld().spawnParticle(
                Particle.GLOW_SQUID_INK,
                end.clone().add(0, 1, 0),
                30,
                0.5, 0.6, 0.5,
                0.08
        );
    }

    private void createAndCallCustomDamageEvent(Player damager, LivingEntity damagee) {
        CustomDamageEvent customDamageEvent = CustomDamageEvent.getBuilder()
                .setDamager(damager)
                .setDamagee(damagee)
                .setCause(CustomDamageEvent.DamageCause.ATTACK)
                .setCauseDisplayName(getName())
                .setLocation(damager.getLocation())
                .setDamage(EntityDamageListener.getDamageFromItemInHand(damager.getInventory().getItemInMainHand()) + calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(damager)))
                .setSendSkillHitToCaster(true)
                .setSendSkillHitToReceiver(true)
                .build();

        customDamageEvent.callEvent();

        if(customDamageEvent.isCancelled()) return;

        new CustomDamageCommand(dc, customDamageEvent).execute();
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        Timer timer = activeMap.remove(uuid);
        if(timer != null) timer.stop();
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerRightClickEvent rightClickEvent)) return AttemptResult.FALSE;

        if(!rightClickEvent.isSword()) return AttemptResult.FALSE;

        if(rightClickEvent.isCancelled()) return AttemptResult.FALSE;

        return super.canUseHook(uuid, event);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.isTick()) return;

        for(UUID uuid : getUsers()) {
            if(!activeMap.containsKey(uuid)) continue;

            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            if(!isItemStackOfSkillType(player.getInventory().getItemInMainHand())) continue;

            double timeLeftInSeconds = activeMap.get(uuid).getTimeLeftInSeconds();
            double timeLeftPercentage = timeLeftInSeconds / RECAST_DURATION;

            ActionBarUtil.sendMessage(
                    player,
                    ComponentUtil.recastDurationRemaining(
                            "Warp",
                            1 - timeLeftPercentage,
                            timeLeftInSeconds),
                    ActionBarPriority.HIGH);
        }
    }

    @EventHandler
    public void onPlayerLeaveSword(PlayerItemHeldEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;

        ItemStack previousItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());

        if(previousItem == null) return;

        if(!ItemUtil.isSword(previousItem.getType())) return;

        clearActionBar(event.getPlayer());
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("cooldown_reduction_per_level"));
            BASE_WARP_DISTANCE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_warp_distance"));
            WARP_DISTANCE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("warp_distance_increase_per_level"));
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_damage"));
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage_increase_per_level"));
            DASH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("dash_strength"));
            RECAST_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("recast_duration"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.active()
                        .append(ComponentUtil.rightClick(true))
                        .append(Component.text("to dash forward.", NamedTextColor.GRAY)),
                30));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("For ", NamedTextColor.GRAY)
                        .append(Component.text(RECAST_DURATION, NamedTextColor.YELLOW))
                        .append(Component.text(RECAST_DURATION == 1.0 ? " second" : " seconds", NamedTextColor.GRAY))
                        .append(Component.text(" after dashing, ", NamedTextColor.GRAY))
                        .append(ComponentUtil.leftClick(false))
                        .append(Component.text(" an enemy within ", NamedTextColor.GRAY))
                        .append(ComponentUtil.skillValuesBasedOnLevel(BASE_WARP_DISTANCE, WARP_DISTANCE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                        .append(Component.text(" blocks to warp into range and strike, dealing ", NamedTextColor.GRAY))
                        .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                        .append(Component.text(" bonus damage.", NamedTextColor.GRAY)),
                30));
        return lore;
    }
}