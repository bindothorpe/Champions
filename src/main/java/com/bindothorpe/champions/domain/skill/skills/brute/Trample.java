package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
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

public class Trample extends Skill {

    private final int TRAMPLE_COUNT = 8;
    private final int TRAMPLE_DELAY_IN_MILLISECONDS = 500;
    private Map<UUID, Long> lastTrampleTimestamps = new HashMap<>();
    private Map<UUID, Integer> activeTramplesRemaining = new HashMap<>();
    private final List<Double> damage = List.of(0.5, 1.0, 1.5);

    public Trample(DomainController dc) {
        super(dc, SkillId.TRAMPLE, SkillId.TRAMPLE.getSkillType(), ClassType.BRUTE, "Trample", List.of(12d, 8d, 5d), 3, 1);
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
        activeTramplesRemaining.put(player.getUniqueId(), TRAMPLE_COUNT - 1);
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
        double radius = 3.0;

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
        Set<Entity> nearby = player.getLocation().getNearbyEntities(3, 1, 3)
                .stream()
                .filter(entity -> !dc.getTeamManager().getTeamFromEntity(player).equals(dc.getTeamManager().getTeamFromEntity(entity)))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(Entity::isOnGround)
                .collect(Collectors.toSet());

        double damage = this.damage.get(getSkillLevel(player.getUniqueId()) - 1);

        for(Entity entity : nearby) {
            CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) entity, player, damage, player.getLocation(), CustomDamageSource.SKILL);
            CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damageEvent).force(0);
            damageEvent.setCommand(customDamageCommand);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if(damageEvent.isCancelled())
                continue;

            customDamageCommand.execute();
        }
    }
}
