package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStartBlockingEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStopBlockingEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerUpdateBlockingEvent;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Evade extends Skill implements ReloadableData {

    private static double COOLDOWN_ON_SUCCESS;
    private static double ACTIVE_DURATION;

    private final Map<UUID, Long> activeBlockingUsersStartTimeMap = new HashMap<>();

    public Evade(DomainController dc) {
        super(dc, "Evade", SkillId.EVADE, SkillType.SWORD, ClassType.ASSASSIN);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        activeBlockingUsersStartTimeMap.remove(uuid);
    }

    @EventHandler
    public void onStartBlocking(PlayerStartBlockingEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(!canUse(uuid, event).result()) return;

        if(activeBlockingUsersStartTimeMap.containsKey(player.getUniqueId())) return;
        activeBlockingUsersStartTimeMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onBlockCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getDamagee() instanceof Player player)) return;
        if(!(event.getDamager() instanceof LivingEntity livingEntity)) return;
        if(!event.getCause().equals(CustomDamageEvent.DamageCause.ATTACK)) return;

        UUID uuid = player.getUniqueId();
        if(!activeBlockingUsersStartTimeMap.containsKey(uuid)) return;

        // Block the attack and start the cooldown
        event.setCancelled(true);
        activeBlockingUsersStartTimeMap.remove(uuid);

        performEvade(player, livingEntity);
    }

    @EventHandler
    public void onUpdateBlocking(PlayerUpdateBlockingEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(!activeBlockingUsersStartTimeMap.containsKey(uuid)) return;

        // Check if the window has passed
        if(event.getBlockDuration() <= ACTIVE_DURATION) return;

        //Fail blocking
        activeBlockingUsersStartTimeMap.remove(uuid);
        startCooldown(uuid);

        ChatUtil.sendMessage(
                player,
                ChatUtil.Prefix.SKILL,
                Component.text("You failed to ").color(NamedTextColor.GRAY)
                        .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.GRAY))
        );
    }

    @EventHandler
    public void onStopBlocking(PlayerStopBlockingEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(!activeBlockingUsersStartTimeMap.containsKey(uuid)) return;

        //Fail blocking
        activeBlockingUsersStartTimeMap.remove(uuid);
        startCooldown(uuid);

        ChatUtil.sendMessage(
                player,
                ChatUtil.Prefix.SKILL,
                Component.text("You failed to ").color(NamedTextColor.GRAY)
                        .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.GRAY))
        );
    }

    private void playerEvadeParticle(@NotNull Player player) {
        player.getWorld().spawnParticle(
                Particle.LARGE_SMOKE,
                player.getLocation().clone().add(0, 0.5, 0),
                1,
                0, 0, 0,
                0
        );
    }

    private void performEvade(Player player, Entity damager) {
        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.TRUE_INVISIBLE, player.getUniqueId(), getNamespacedKey(player), 1, 0.3);

        playerEvadeParticle(player);
        if(!player.isSneaking()) {
            player.teleport(getLocationBehindEntity(damager));
        } else {
            player.teleport(getLocationInFrontEntity(damager, player.getLocation().getDirection()));
        }

        ChatUtil.sendMessage(
                player,
                ChatUtil.Prefix.SKILL,
                Component.text("You evaded ").color(NamedTextColor.GRAY)
                        .append(Component.text(damager.getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text("'s attack.").color(NamedTextColor.GRAY))
        );
        startCooldown(player.getUniqueId(), COOLDOWN_ON_SUCCESS);
    }

    private Location getLocationBehindEntity(Entity entity) {
        Location entityLoc = entity.getLocation();
        Vector direction = entityLoc.getDirection().normalize().multiply(-1);

        // Start checking from 2 blocks away and move closer
        for (double distance = 2.0; distance >= 0.5; distance -= 0.25) {
            Location location = entityLoc.clone();
            location.add(direction.clone().multiply(distance));
            location.setY(entityLoc.getY() + 0.1);

            if (isLocationPassable(location)) {
                return location;
            }
        }

        // Fallback: return entity's current location if no valid spot found
        return entityLoc.clone();
    }

    private Location getLocationInFrontEntity(Entity entity, Vector facingDirection) {
        Location entityLoc = entity.getLocation();
        Vector direction = entityLoc.getDirection().normalize();

        // Start checking from 1 block away and move closer
        for (double distance = 1.0; distance >= 0.5; distance -= 0.25) {
            Location location = entityLoc.clone();
            location.add(direction.clone().multiply(distance));
            location.setY(entityLoc.getY() + 0.1);
            location.setDirection(facingDirection.multiply(2));

            if (isLocationPassable(location)) {
                return location;
            }
        }

        // Fallback: return entity's current location if no valid spot found
        Location fallback = entityLoc.clone();
        fallback.setDirection(facingDirection.multiply(2));
        return fallback;
    }

    private boolean isLocationPassable(Location location) {
        if (location.getWorld() == null) {
            return false;
        }

        Block block = location.getBlock();
        Block blockAbove = location.clone().add(0, 1, 0).getBlock();

        // Check if the location and block above are passable (air or non-solid)
        boolean feetPassable = block.isPassable() || block.getType().isAir();
        boolean headPassable = blockAbove.isPassable() || blockAbove.getType().isAir();

        return feetPassable && headPassable;
    }


    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.cooldown_reduction_per_level");
            COOLDOWN_ON_SUCCESS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.cooldown_on_success");
            ACTIVE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.active_duration");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
