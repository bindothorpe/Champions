package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Blink extends Skill implements ReloadableData {

    private final Map<UUID, Timer> recastTimerMap = new HashMap<>();
    private final Map<UUID, Location> recastLocationMap = new HashMap<>();

    private double BASE_DISTANCE;
    private double DISTANCE_INCREASE_PER_LEVEL;
    private double RECAST_DURATION;

    public Blink(DomainController dc) {
        super(dc, "Blink", SkillId.BLINK, SkillType.AXE, ClassType.ASSASSIN);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();;
        if(recastTimerMap.containsKey(uuid)) {
            performDeBlink(uuid);
            return;
        }

        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        performBlink(uuid);
    }

    private void performBlink(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if(player == null) return;

        double maxDistance = calculateBasedOnLevel(BASE_DISTANCE, DISTANCE_INCREASE_PER_LEVEL, getSkillLevel(uuid));
        double currentDistance = 0;

        Location startingLocation = player.getLocation();
        Location targetLocation = player.getLocation();

        while(currentDistance <= maxDistance) {
            Location location = startingLocation.clone().add(0, 0.2, 0).add(player.getLocation().getDirection().multiply(currentDistance));

            //Exit the loop
            if(!location.getBlock().isPassable() || !location.getBlock().getRelative(BlockFace.UP).isPassable()) break;

            currentDistance += 1.0;
            targetLocation = location;
        }

        Timer timeoutTimer = new Timer(dc.getPlugin(), RECAST_DURATION, () -> {
            recastTimerMap.remove(uuid);
            recastLocationMap.remove(uuid);
            ChatUtil.sendMessage(player, ChatUtil.Prefix.SKILL, Component.text("You cannot use ").color(NamedTextColor.GRAY)
                    .append(Component.text("De-blink").color(NamedTextColor.YELLOW))
                    .append(Component.text(" anymore.").color(NamedTextColor.GRAY)));
        });

        timeoutTimer.start();

        recastTimerMap.put(uuid, timeoutTimer);
        recastLocationMap.put(uuid, startingLocation);
        player.teleport(targetLocation);

        spawnParticleLine(startingLocation, player.getLocation());

        player.setFallDistance(0);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_BLINK);
    }

    private void performDeBlink(UUID uuid) {
        Timer timer = recastTimerMap.remove(uuid);
        if(timer == null) return;

        timer.stop();
        recastTimerMap.remove(uuid);
        Location location = recastLocationMap.remove(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;

        spawnParticleLine(player.getLocation(), location);

        player.teleport(location);
        player.setFallDistance(0);
        ChatUtil.sendSkillMessage(player, "De-blink", getSkillLevel(player));
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_BLINK);
    }

    private void spawnParticleLine(@NotNull Location startLocation, @NotNull Location endLocation) {
        if(startLocation.getWorld() != endLocation.getWorld()) return;

        World world = startLocation.getWorld();

        Set<Vector> points = ShapeUtil.line(startLocation.toVector(), endLocation.toVector(), 0.5);

        for(Vector point : points) {
            startLocation.getWorld().spawnParticle(
                    Particle.LARGE_SMOKE,
                    new Location(world, point.getX(), point.getY(), point.getZ()),
                    3,      // spawn 3 particles for a thicker cloud
                    0.1,    // slight X spread
                    0.1,    // slight Y spread
                    0.1,    // slight Z spread
                    0.0     // no velocity
            );
        }
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {

        if(!(event instanceof PlayerRightClickEvent rightClickEvent)) return false;

        if(!rightClickEvent.isAxe()) return false;

        if(dc.getStatusEffectManager().hasStatusEffect(StatusEffectType.SLOW, uuid)) return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("cooldown_reduction_per_level"));
            BASE_DISTANCE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_distance"));
            DISTANCE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("distance_increase_per_level"));
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
        return List.of();
    }
}
