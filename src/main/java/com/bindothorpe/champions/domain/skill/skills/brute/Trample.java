package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.interact.PlayerDropItemWrapperEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class Trample extends Skill implements ReloadableData {

    private static int BASE_TRAMPLE_COUNT;
    private static int TRAMPLE_COUNT_INCREASE_PER_LEVEL;
    private static double BASE_TRAMPLE_RADIUS;
    private static double TRAMPLE_RADIUS_INCREASE_PER_LEVEL;
    private static int TRAMPLE_DELAY_IN_MILLISECONDS;
    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;

    private final Map<UUID, Long> lastTrampleTimestamps = new HashMap<>();
    private final Map<UUID, Integer> activeTramplesRemaining = new HashMap<>();

    public Trample(DomainController dc) {
        super(dc, "Trample", SkillId.TRAMPLE, SkillId.TRAMPLE.getSkillType(), ClassType.BRUTE);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        lastTrampleTimestamps.remove(uuid);
        activeTramplesRemaining.remove(uuid);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemWrapperEvent event) {

        if(!(event.isSword() || event.isAxe()))
            return;

        boolean activateResult = activate(event.getPlayer().getUniqueId(), event);

        if(!activateResult)
            return;

        Player player = event.getPlayer();

        performTrample(player);
        int trampleCount = calculateBasedOnLevel(BASE_TRAMPLE_COUNT, TRAMPLE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));
        activeTramplesRemaining.put(player.getUniqueId(), trampleCount - 1);
        lastTrampleTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        for(Map.Entry<UUID, Integer> entry : activeTramplesRemaining.entrySet()) {
            UUID uuid = entry.getKey();
            int remaining = entry.getValue();

            // Get the last time this user has used trample instance
            long lastTrampleTimestamp = lastTrampleTimestamps.get(uuid);

            // If the last trample timestamp + the delay is smaller than the current time in millis, do nothing.
            if(lastTrampleTimestamp + TRAMPLE_DELAY_IN_MILLISECONDS > System.currentTimeMillis())
                continue;

            Player player = Bukkit.getPlayer(uuid);

            performTrample(player);

            remaining -= 1;

            activeTramplesRemaining.put(player.getUniqueId(), remaining);
            lastTrampleTimestamps.put(player.getUniqueId(), System.currentTimeMillis());

            // Clean up
            if(remaining <= 0) {
                activeTramplesRemaining.remove(uuid);
                lastTrampleTimestamps.remove(uuid);
            }
        }
    }

    private void performTrample(Player player) {
        // Send a message and play a sound
        dc.getSoundManager().playSound(player, CustomSound.SKILL_TRAMPLE);

        // Create particle effect using ShapeUtil
        Location center = player.getLocation();
        double radius = calculateBasedOnLevel(BASE_TRAMPLE_RADIUS, TRAMPLE_RADIUS_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));

        // Get circle points using ShapeUtil
        Set<Vector> circlePoints = ShapeUtil.circle(radius);

        // Create circle of particles at ground level
        for (Vector point : circlePoints) {
            Location particleLoc = center.clone().add(point.getX(), 0.1, point.getZ());

            // Spawn block crack particles (using DIRT as the block - you can change this)
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRUMBLE,
                    particleLoc,
                    5, // Amount per point
                    0.1, 0, 0.1, // Spread
                    0, // Speed
                    Material.DIRT.createBlockData() // Block type to crack
            );
        }

        // Damage entities (your existing code)
        Set<Entity> nearby = player.getLocation().getNearbyEntities(radius, 1, radius)
                .stream()
                .filter(entity -> !dc.getTeamManager().getTeamFromEntity(player).equals(dc.getTeamManager().getTeamFromEntity(entity)))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(Entity::isOnGround)
                .collect(Collectors.toSet());

        double damage = calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));

        for(Entity entity : nearby) {
            CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) entity, player, damage, player.getLocation(), CustomDamageSource.SKILL, getName());
            CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damageEvent).force(0);
            damageEvent.setCommand(customDamageCommand);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if(damageEvent.isCancelled())
                continue;

            customDamageCommand.execute();
        }
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.trample.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.trample.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.trample.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.trample.cooldown_reduction_per_level");
            BASE_TRAMPLE_COUNT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.trample.base_trample_count");
            TRAMPLE_COUNT_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.trample.trample_count_increase_per_level");
            BASE_TRAMPLE_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.trample.base_trample_radius");
            TRAMPLE_RADIUS_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.trample.trample_radius_increase_per_level");
            TRAMPLE_DELAY_IN_MILLISECONDS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.trample.trample_delay_in_milliseconds");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.trample.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.trample.damage_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}