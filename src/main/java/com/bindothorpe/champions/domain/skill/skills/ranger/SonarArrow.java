package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
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

public class SonarArrow extends Skill {

    private final Set<UUID> primed = new HashSet<>();
    private final Set<Arrow> particleTrail = new HashSet<>();

    private final double range = 10;

    public SonarArrow(DomainController dc) {
        super(dc, SkillId.SONAR_ARROW, SkillType.BOW, ClassType.RANGER, "Sonar Arrow", Arrays.asList(10.0), 1, 2);
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerLeftClickEvent event) {
        boolean success = activate(event.getPlayer().getUniqueId(), event);

        if (!success) {
            return;
        }

        primed.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        if (!primed.contains(player.getUniqueId()))
            return;


        arrow.setMetadata("sonar", new FixedMetadataValue(dc.getPlugin(), true));
        if(player.isSneaking())
            arrow.setMetadata("bounce", new FixedMetadataValue(dc.getPlugin(), true));
        primed.remove(player.getUniqueId());

        particleTrail.add(arrow);

    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!arrow.hasMetadata("sonar"))
            return;

        Player player = (Player) arrow.getShooter();

        Block block = event.getHitBlock();

        particleTrail.remove(arrow);

        if(block != null && arrow.hasMetadata("bounce")) {

            event.setCancelled(true);

            performBounce(arrow, event, player);

            return;
        }

        performSonar(player, arrow, event.getHitEntity());


    }

    private void performBounce(Arrow arrow, ProjectileHitEvent event, Player player) {
        arrow.removeMetadata("bounce", dc.getPlugin());
        Vector velocity = arrow.getVelocity();

        BlockFace faceHit = event.getHitBlockFace();
        if (faceHit == BlockFace.EAST || faceHit == BlockFace.WEST) {
            velocity.setX(-velocity.getX());
        } else if (faceHit == BlockFace.NORTH || faceHit == BlockFace.SOUTH) {
            velocity.setZ(-velocity.getZ());
        } else if (faceHit == BlockFace.UP || faceHit == BlockFace.DOWN) {
            velocity.setY(-velocity.getY());
        }

        arrow.remove();

        // Spawn the new arrow at the location where the old one hit
        Arrow bouncingArrow = (Arrow) arrow.getWorld().spawnEntity(arrow.getLocation(), EntityType.ARROW);
        bouncingArrow.setVelocity(velocity.multiply(0.8));
        bouncingArrow.setShooter(player);

        bouncingArrow.setMetadata("sonar", new FixedMetadataValue(dc.getPlugin(), true));

        particleTrail.add(bouncingArrow);
    }

    private void performSonar(Player player, Arrow arrow, Entity hit) {
        new BukkitRunnable() {
            int x = 0;
            Set<Entity> entities = new HashSet<>();
            List<Double> ranges = Arrays.asList(5D, 7.5D, 10D);
            int devider = 64;

            @Override
            public void run() {
                x++;
                if (x == 4) {
                    cancel();
                    return;
                }


                new BukkitRunnable() {
                    int y = 3;

                    @Override
                    public void run() {
                        if (y == 0) {
                            cancel();
                            return;
                        }

                        double range = ranges.get(ranges.size() - y);
                        Set<Vector> points = ShapeUtil.sphere(range, false, 0, devider / y);

                        Location loc = arrow.getLocation();
                        if (hit != null) {
                            loc = hit.getLocation();
                        }

                        // Spawn particles
                        for (Vector point : points) {
                            Particle.DustOptions dustOptions = new Particle.DustOptions(dc.getTeamManager().getTeamFromEntity(player).getColor(), 1);
                            loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(point), 1, 0, 0, 0, 0, dustOptions, true);
                        }

                        // Find entities
                        for (Entity entity : arrow.getNearbyEntities(range, range, range)) {

                            // Skip if already glowing
                            if (entities.contains(entity))
                                continue;

                            // Skip if not living
                            if (!(entity instanceof LivingEntity))
                                continue;

                            LivingEntity livingEntity = (LivingEntity) entity;

                            // Skip if on same team
                            if (dc.getTeamManager().getTeamFromEntity(player).equals(dc.getTeamManager().getTeamFromEntity(livingEntity)))
                                continue;

                            livingEntity.setGlowing(true);
                            entities.add(livingEntity);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    livingEntity.setGlowing(false);
                                }
                            }.runTaskLater(dc.getPlugin(), 2 * 20L);
                        }

                        y -= 1;
                    }
                }.runTaskTimer(dc.getPlugin(), 0, 10L / 3);

                entities.clear();


            }
        }.runTaskTimer(dc.getPlugin(), 0, 3 * 20L);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getUpdateType() != UpdateType.TICK)
            return;

        for (Arrow arrow : particleTrail) {
            Location loc = arrow.getLocation();
            Particle.DustOptions dustOptions = new Particle.DustOptions(dc.getTeamManager().getTeamFromEntity((Player) arrow.getShooter()).getColor(), 1);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions, true);
        }
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerLeftClickEvent))
            return false;

        PlayerLeftClickEvent e = (PlayerLeftClickEvent) event;


        if (dc.getTeamManager().getTeamFromEntity(e.getPlayer()) == null) {
            return false;
        }


        if (!e.isBow())
            return false;


        if (primed.contains(uuid))
            return false;


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
                .append(ComponentUtil.skillLevelValues(skillLevel, Arrays.asList(range), NamedTextColor.YELLOW))
                .append(Component.text(" block").color(NamedTextColor.GRAY)));
        lore.add(Component.text("radius").color(NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.add(Component.text("Sneaking").color(NamedTextColor.YELLOW)
                .append(Component.text(" while shooting the").color(NamedTextColor.GRAY)));
        lore.add(Component.text("arrow will make it bounce off").color(NamedTextColor.GRAY));
        lore.add(Component.text("of the first block it hits").color(NamedTextColor.GRAY));

        return lore;
    }
}
