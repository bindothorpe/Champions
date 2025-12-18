package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.domain.skill.subSkills.PrimeArrowSkill;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SonarArrow extends PrimeArrowSkill implements ReloadableData {

    private final Set<Arrow> bouncingArrows = new HashSet<>();

    private static double DETECTION_RADIUS;
    private static double BOUNCE_STRENGTH_MULT;

    public SonarArrow(DomainController dc) {
        super(dc, "Sonar Arrow", SkillId.SONAR_ARROW, SkillType.BOW, ClassType.RANGER);
    }

    @Override
    protected void onSkillArrowLaunch(Arrow arrow, Player shooter) {
        if (shooter.isSneaking()) {
            bouncingArrows.add(arrow);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;

        if (!isArrowOfSkill(arrow))
            return;

        Player player = (Player) arrow.getShooter();

        Block block = event.getHitBlock();

        arrows.remove(arrow);

        if(block != null && bouncingArrows.contains(arrow)) {
            event.setCancelled(true);
            bouncingArrows.remove(arrow);
            performBounce(arrow, event, player);
            return;
        }

        performSonar(player, arrow, event.getHitEntity());
    }

    private Vector getBounceVelocity(Vector startingVelocity, BlockFace hitFace) {
        if (hitFace == BlockFace.EAST || hitFace == BlockFace.WEST) {
            startingVelocity.setX(-startingVelocity.getX());
        } else if (hitFace == BlockFace.NORTH || hitFace == BlockFace.SOUTH) {
            startingVelocity.setZ(-startingVelocity.getZ());
        } else if (hitFace == BlockFace.UP || hitFace == BlockFace.DOWN) {
            startingVelocity.setY(-startingVelocity.getY());
        }
        return startingVelocity;
    }

    private void performBounce(Arrow arrow, ProjectileHitEvent event, Player player) {
        if(event.getHitBlock() == null) return;

        BlockFace hitFace = event.getHitBlockFace();
        Vector velocity = getBounceVelocity(arrow.getVelocity(), hitFace);

        // Offset the spawn location away from the block face
        Location spawnLocation = arrow.getLocation().clone();
        Vector offset = hitFace.getDirection().multiply(0.5); // Move 0.5 blocks away from the wall
        spawnLocation.add(offset);

        arrow.remove();

        // Spawn the new arrow at the offset location
        Arrow bouncingArrow = (Arrow) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ARROW);
        bouncingArrow.setVelocity(velocity.multiply(BOUNCE_STRENGTH_MULT));
        bouncingArrow.setShooter(player);
        bouncingArrow.setPickupStatus(Arrow.PickupStatus.CREATIVE_ONLY);

        setArrowOfSkill(bouncingArrow, true);
        arrows.add(bouncingArrow);

        Location soundLocation = event.getHitBlock().getLocation();
        dc.getSoundManager().playSound(soundLocation, CustomSound.SKILL_RANGER_SONAR_ARROW_BOUNCE);
    }

    private void performSonar(Player player, Arrow arrow, Entity hit) {
        new BukkitRunnable() {
            int pulse = 0;

            @Override
            public void run() {
                pulse++;
                if (pulse == 4) {
                    cancel();
                    return;
                }

                dc.getSoundManager().playSound(player, CustomSound.SKILL_RANGER_SONAR_ARROW_SCAN);
                runSonarScan(player, arrow, hit, pulse);
            }
        }.runTaskTimer(dc.getPlugin(), 0, 3 * 20L);
    }

    private void runSonarScan(Player player, Arrow arrow, Entity hit, int pulseNumber) {
        Set<Entity> detectedEntities = new HashSet<>();
        List<Double> ranges = getDetectionRadiusList(3);
        int divider = 64;

        new BukkitRunnable() {
            int wave = 3;

            @Override
            public void run() {
                if (wave == 0) {
                    cancel();
                    return;
                }

                double range = ranges.get(ranges.size() - wave);
                Set<Vector> points = ShapeUtil.sphere(range, false, 0, divider / wave);

                Location loc = arrow.getLocation();
                if (hit != null) {
                    loc = hit.getLocation();
                }

                // Spawn particles
                spawnSonarParticles(loc, points, player);

                // Find and mark entities
                boolean entityDetected = detectAndMarkEntities(player, arrow, loc, range, detectedEntities);

                if (entityDetected) {
                    dc.getSoundManager().playSound(player, CustomSound.SKILL_RANGER_SONAR_ARROW_DETECT);
                }

                wave--;
            }
        }.runTaskTimer(dc.getPlugin(), 0, 10L / 3);
    }

    private void spawnSonarParticles(Location center, Set<Vector> points, Player player) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(
                dc.getTeamManager().getTeamFromEntity(player).getColor(),
                1
        );

        for (Vector point : points) {
            center.getWorld().spawnParticle(
                    Particle.DUST,
                    center.clone().add(point),
                    1, 0, 0, 0, 0,
                    dustOptions,
                    true
            );
        }
    }

    private boolean detectAndMarkEntities(Player player, Arrow arrow, Location center,
                                          double range, Set<Entity> alreadyDetected) {
        boolean foundNewEntity = false;

        for (Entity entity : arrow.getNearbyEntities(range, range, range)) {
            // Skip if already detected
            if (alreadyDetected.contains(entity))
                continue;

            // Skip if not living
            if (!(entity instanceof LivingEntity))
                continue;

            LivingEntity livingEntity = (LivingEntity) entity;

            // Skip if on same team
            if (dc.getTeamManager().getTeamFromEntity(player)
                    .equals(dc.getTeamManager().getTeamFromEntity(livingEntity)))
                continue;

            // Mark entity as detected
            livingEntity.setGlowing(true);
            alreadyDetected.add(livingEntity);
            foundNewEntity = true;

            // Schedule glow removal
            scheduleGlowRemoval(livingEntity);
        }

        return foundNewEntity;
    }

    private void scheduleGlowRemoval(LivingEntity entity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                entity.setGlowing(false);
            }
        }.runTaskLater(dc.getPlugin(), 2 * 20L);
    }



    private static List<Double> getDetectionRadiusList(int divisions) {
        List<Double> radius_list = new ArrayList<>();
        for(int i = 0; i < divisions; i++) {
            radius_list.add(DETECTION_RADIUS / divisions * (i + 1));
        }
        return radius_list;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getUpdateType() != UpdateType.TICK)
            return;

        for (Arrow arrow : arrows) {
            Location loc = arrow.getLocation();
            Particle.DustOptions dustOptions = new Particle.DustOptions(dc.getTeamManager().getTeamFromEntity((Player) arrow.getShooter()).getColor(), 1);
            loc.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dustOptions, true);
        }
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerLeftClickEvent e))
            return AttemptResult.FALSE;


        if (dc.getTeamManager().getTeamFromEntity(e.getPlayer()) == null) {
            return AttemptResult.FALSE;
        }


        if (!e.isBow())
            return AttemptResult.FALSE;

        if (primed.contains(uuid))
            return new AttemptResult(
                    false,
                    Component.text("You have already primed ", NamedTextColor.GRAY)
                            .append(Component.text(getName(), NamedTextColor.YELLOW))
                            .append(Component.text(".", NamedTextColor.GRAY)),
                    ChatUtil.Prefix.SKILL
            );


        return super.canUseHook(uuid, event);
    }



    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(ComponentUtil.active()
                .append(ComponentUtil.leftClick())
                .append(Component.text("to prime").color(NamedTextColor.GRAY)));
        lore.add(Component.text("your next arrow").color(NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.add(Component.text("When the arrow hits a block").color(NamedTextColor.GRAY));
        lore.add(Component.text("or an enemy, it will emit a").color(NamedTextColor.GRAY));
        lore.add(Component.text("sonar pulse, revealing all").color(NamedTextColor.GRAY));
        lore.add(Component.text("enemies in a ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, List.of(DETECTION_RADIUS), NamedTextColor.YELLOW))
                .append(Component.text(" block").color(NamedTextColor.GRAY)));
        lore.add(Component.text("radius").color(NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.add(Component.text("Sneaking").color(NamedTextColor.YELLOW)
                .append(Component.text(" while shooting the").color(NamedTextColor.GRAY)));
        lore.add(Component.text("arrow will make it bounce off").color(NamedTextColor.GRAY));
        lore.add(Component.text("of the first block it hits").color(NamedTextColor.GRAY));

        return lore;
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.sonar_arrow.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.sonar_arrow.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.sonar_arrow.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.sonar_arrow.cooldown_reduction_per_level");
            DETECTION_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.sonar_arrow.detection_radius");
            BOUNCE_STRENGTH_MULT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.sonar_arrow.bounce_strength_mult");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
