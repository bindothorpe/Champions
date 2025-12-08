package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.EntityUtil;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class HeavySwing extends ChargeSkill {
    private final List<Double> baseDamage = List.of(2d, 3d, 4d);
    private final List<Double> minConeAngle = List.of(45d, 45d, 45d);
    private final List<Double> maxConeAngle = List.of(90d, 90d, 90d);
    private final List<Double> minRange = List.of(2d, 3d, 4d);
    private final List<Double> maxRange = List.of(5d, 6d, 7d);

    public HeavySwing(DomainController dc) {
        super(dc, SkillId.HEAVY_SWING, SkillType.SWORD, ClassType.KNIGHT, "Heavy Swing",
                List.of(5d, 4d, 3d), 3, 1, List.of(40, 35, 30), List.of(5d, 5d, 5d));
    }

    /**
     * Calculates the signed angle of a vector relative to the central direction
     * Used for rotational sorting of cone points
     */
    private double calculateAngleFromCenter(Vector vector, Vector centerDirection) {
        // Normalize both vectors for accurate angle calculation
        Vector v1 = vector.clone().normalize();
        Vector v2 = centerDirection.clone().normalize();

        // Calculate the angle between the vectors
        double dot = Math.max(-1.0, Math.min(1.0, v1.dot(v2))); // Clamp to avoid floating point errors
        double angle = Math.acos(dot);

        // Use cross product to determine which side of center this vector is on
        Vector cross = v2.crossProduct(v1);

        // If the Y component of cross product is negative, it's on the "left" side, make angle negative
        if (cross.getY() < 0) {
            angle = -angle;
        }

        return angle;
    }

    /**
     * Rotates a vector around an arbitrary axis by the given angle
     * Uses Rodrigues' rotation formula
     */
    private Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
        axis = axis.normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vector rotated = vector.clone().multiply(cos);
        rotated.add(axis.clone().crossProduct(vector).multiply(sin));
        rotated.add(axis.clone().multiply(axis.dot(vector) * (1 - cos)));

        return rotated;
    }

    private void executeHeavySwing(Player player, int charge) {
        int skillLevel = getSkillLevel(player.getUniqueId());
        double chargePercentage = Math.min((double) charge / getMaxCharge(player.getUniqueId()), 1.0);

        // Calculate damage (50% to 100% based on charge)
        double damageMultiplier = 0.5 + (0.5 * chargePercentage);
        double finalDamage = baseDamage.get(skillLevel - 1) * damageMultiplier;

        // Calculate cone angle (min to max based on charge)
        double minAngle = minConeAngle.get(skillLevel - 1);
        double maxAngle = maxConeAngle.get(skillLevel - 1);
        double coneAngle = minAngle + ((maxAngle - minAngle) * chargePercentage);

        // Calculate range (min to max based on charge)
        double minRangeValue = minRange.get(skillLevel - 1);
        double maxRangeValue = maxRange.get(skillLevel - 1);
        double finalRange = minRangeValue + ((maxRangeValue - minRangeValue) * chargePercentage);

        // Get player's full 3D looking direction (including vertical angle)
        Vector direction = player.getLocation().getDirection().clone().normalize();

        // Generate cone area in 2D first (XZ plane) with higher density
        Vector horizontalDirection = direction.clone();
        horizontalDirection.setY(0);
        horizontalDirection.normalize();
        Set<Vector> coneVectors2D = ShapeUtil.cone2D(0.5, finalRange, coneAngle, horizontalDirection, 2); // Higher density

        // Calculate rotation angles to align cone with player's 3D looking direction
        double pitch = Math.asin(direction.getY()); // Player's vertical angle

        // Rotate each 2D cone vector to match the player's 3D looking direction
        List<Vector> coneVectors3D = new ArrayList<>();
        for (Vector vector2D : coneVectors2D) {
            // Rotate around the horizontal axis to apply pitch
            Vector rotated = rotateAroundAxis(vector2D, horizontalDirection.clone().crossProduct(new Vector(0, 1, 0)).normalize(), pitch);
            coneVectors3D.add(rotated);
        }

        // Sort vectors by their angle relative to player's looking direction (rotational sweep)
        coneVectors3D.sort((v1, v2) -> {
            // Calculate angle of each vector relative to the central direction
            double angle1 = calculateAngleFromCenter(v1, direction);
            double angle2 = calculateAngleFromCenter(v2, direction);
            return Double.compare(angle1, angle2);
        });

        // Play activation sound
        dc.getSoundManager().playSound(player, CustomSound.SKILL_HEAVY_SWING);

        // Track damaged entities to avoid multiple hits
        Set<UUID> damagedEntities = new HashSet<>();

        // Start from player's head/eye level
        Location playerHeadLocation = player.getEyeLocation().clone().add(0, -0.5, 0);

        // Animate the slash from left to right
        animateSlashEffect(coneVectors3D, playerHeadLocation, player, finalDamage, damagedEntities);
    }

    private void animateSlashEffect(List<Vector> coneVectors, Location playerHeadLocation, Player player,
                                    double finalDamage, Set<UUID> damagedEntities) {

        // Split vectors into waves for animation (adjust wave size for smoother/choppier animation)
        int waveSize = Math.max(1, coneVectors.size() / 2); // 10 waves total
        int waveCount = (int) Math.ceil((double) coneVectors.size() / waveSize);

        for (int wave = 0; wave < waveCount; wave++) {
            int startIndex = wave * waveSize;
            int endIndex = Math.min(startIndex + waveSize, coneVectors.size());
            List<Vector> waveVectors = coneVectors.subList(startIndex, endIndex);

            // Delay each wave by 1 tick (50ms) for smooth animation
            new BukkitRunnable() {
                @Override
                public void run() {
                    processWaveVectors(waveVectors, playerHeadLocation, player, finalDamage, damagedEntities);
                }
            }.runTaskLater(dc.getPlugin(), wave); // wave * 1 tick delay
        }
    }

    private void processWaveVectors(List<Vector> waveVectors, Location playerHeadLocation, Player player,
                                    double finalDamage, Set<UUID> damagedEntities) {

        // Process each location in the wave
        for (Vector offset : waveVectors) {
            Location targetLocation = playerHeadLocation.clone().add(offset);

            // Spawn particles at each location
            player.getWorld().spawnParticle(
                    Particle.BLOCK_CRUMBLE,
                    targetLocation,
                    1,
                    0, 0, 0,
                    0,
                    Material.IRON_BLOCK.createBlockData());



            // Check for entities at this location
            Collection<Entity> nearbyEntities = targetLocation.getWorld()
                    .getNearbyEntities(targetLocation, 0.8, 2.0, 0.8);

            for (Entity entity : nearbyEntities) {
                if (!(entity instanceof LivingEntity)) continue;
                if (entity.equals(player)) continue;
                if (damagedEntities.contains(entity.getUniqueId())) continue;

                LivingEntity target = (LivingEntity) entity;

                // Create damage event
                CustomDamageEvent damageEvent = new CustomDamageEvent(dc, target, player, finalDamage,
                        playerHeadLocation, CustomDamageSource.SKILL, getName());
                CustomDamageCommand damageCommand = new CustomDamageCommand(dc, damageEvent);
                damageEvent.setCommand(damageCommand);

                Bukkit.getPluginManager().callEvent(damageEvent);

                if (!damageEvent.isCancelled()) {
                    damageCommand.execute();

                    // Apply knockback
                    Vector knockbackDirection = target.getLocation().toVector()
                            .subtract(playerHeadLocation.toVector()).normalize();
                    knockbackDirection.setY(0.2); // Vertical knockback
                    knockbackDirection.multiply(1.0); // Horizontal knockback

                    target.setVelocity(target.getVelocity().add(knockbackDirection));

                    damagedEntities.add(entity.getUniqueId());
                }
            }
        }
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        // Auto-release when max charge duration is reached
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        if (activate(uuid, null)) {
            executeHeavySwing(player, charge);
        }
    }

    @Override
    protected void onMaxChargeReached(UUID uuid, int charge) {
        // Nothing special happens when max charge is reached, just continue charging
    }

    @Override
    protected void onCharge(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        // Play charging sound
        dc.getSoundManager().playSound(player, CustomSound.CHARGE_SKILL_CHARGE, getChargePercentage(uuid));

        // Show charge progress in action bar
        ChatUtil.sendActionBarMessage(player, ComponentUtil.skillCharge(getName(), true, charge, getMaxCharge(uuid)));
    }

    @Override
    protected void onChargeStart(UUID uuid) {
        // Nothing special needed when charging starts
    }

    @Override
    protected void onChargeEnd(UUID uuid, int charge) {
        // Execute the attack when charge is released
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        if (activate(uuid, null)) {
            executeHeavySwing(player, charge);
        }
    }

    @Override
    protected void onUpdate(UUID uuid) {
        // Nothing needed in the update loop for this skill
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();

        lore.add(ComponentUtil.active()
                .append(Component.text("Hold down ").color(NamedTextColor.GRAY))
                .append(ComponentUtil.rightClick())
                .append(Component.text("to charge.").color(NamedTextColor.GRAY)));

        lore.add(Component.text("Let go of right-click to release a").color(NamedTextColor.GRAY));
        lore.add(Component.text("powerful slash attack in front of").color(NamedTextColor.GRAY));
        lore.add(Component.text("you that deals ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, baseDamage, NamedTextColor.YELLOW))
                .append(Component.text(" damage").color(NamedTextColor.GRAY)));

        lore.add(Component.text("to all enemies within ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel,
                        Arrays.asList("2~5", "3~6", "4~7"), NamedTextColor.YELLOW))
                .append(Component.text(" blocks").color(NamedTextColor.GRAY)));

        return lore;
    }
}