package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;

public class LotusStrapItem extends GameItem {

    private boolean isTriggered = false;
    private boolean isActive = false;
    private Timer timer = null;
    private final double activateDelay;
    private final double triggerDelay;
    private final Timer timeoutTimer;
    private Set<Vector> circlePoints;
    private Set<Vector> largeCirclePoints;
    private final double explosionRadius;
    private final double explosionDamage;
    private final double slowModifier;
    private final boolean showRemainingDuration;

    private TextDisplay textDisplay;
    private BlockDisplay blockDisplay;

    public LotusStrapItem(DomainController dc, Player owner, double collisionRadius, double activeDuration, double activateDelay, double triggerDelay, double explosionRadius, double explosionDamage, double slowModifier, boolean showRemainingDuration) {
        super(dc, Material.SPORE_BLOSSOM, -1, owner, collisionRadius, 0.15, BlockCollisionMode.TOP_ONLY);
        this.activateDelay = activateDelay;
        timeoutTimer = new Timer(dc.getPlugin(), activeDuration,
                this::remove);
        this.explosionRadius = explosionRadius;
        this.triggerDelay = triggerDelay;
        this.explosionDamage = explosionDamage;
        this.slowModifier = slowModifier;
        this.showRemainingDuration = showRemainingDuration;
    }

    public LotusStrapItem(DomainController dc, Player owner, double collisionRadius, double activeDuration, double activateDelay, double triggerDelay, double explosionRadius, double explosionDamage, double slowModifier) {
        this(dc, owner, collisionRadius, activeDuration, activateDelay, triggerDelay, explosionRadius,explosionDamage, slowModifier, false);
    }

    private void handleTrail() {
        Location loc = getItem().getLocation();
        getItem().getWorld().spawnParticle(
                Particle.DUST,
                dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner()),
                null,
                loc.x(),
                loc.y(),
                loc.z(),
                1,     // count
                0.0,   // offsetX - random spread in X direction
                0.0,   // offsetY - random spread in Y direction
                0.0,   // offsetZ - random spread in Z direction
                0.0,  // extra (speed) - gives particles initial velocity
                new Particle.DustOptions(Color.FUCHSIA, 1));
    }

    private void spawnLargeCircleParticles() {
        largeCirclePoints().forEach(
                point -> getItem().getWorld().spawnParticle(Particle.DUST, getItem().getLocation().clone().add(point), 1, new Particle.DustOptions(Color.FUCHSIA, 1))
        );
        for(Entity entity : getItem().getNearbyEntities(explosionRadius, explosionRadius, explosionRadius)) {
            if(!(entity instanceof LivingEntity livingEntity)) continue;
            if(!dc.getTeamManager().areEntitiesOnDifferentTeams(livingEntity, getOwner())) continue;
            dc.getEntityStatusManager().addEntityStatus(livingEntity.getUniqueId(),
                    new EntityStatus(
                            EntityStatusType.MOVEMENT_SPEED,
                            -slowModifier,
                            triggerDelay,
                            false,
                            false,
                            this
                    ));
            dc.getEntityStatusManager().updateEntityStatus(livingEntity.getUniqueId(), EntityStatusType.MOVEMENT_SPEED);
        }
    }

    private void spawnSmallCircleParticles() {
        circlePoints().forEach(
                point -> {
                    Location loc = getItem().getLocation().add(point);
                    getItem().getWorld().spawnParticle(
                            Particle.DUST,
                            dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner()),
                            null,
                            loc.x(),
                            loc.y(),
                            loc.z(),
                            1,     // count
                            0.0,   // offsetX - random spread in X direction
                            0.0,   // offsetY - random spread in Y direction
                            0.0,   // offsetZ - random spread in Z direction
                            0.0,  // extra (speed) - gives particles initial velocity
                            new Particle.DustOptions(Color.FUCHSIA, 1));
                }
        );

    }

    @Override
    public void onTickUpdate() {
        if(!isActive) {
            handleTrail();
            return;
        }

        if(isTriggered) {
            spawnLargeCircleParticles();
        } else {
            spawnSmallCircleParticles();
            if(showRemainingDuration) updateTimer();
        }


    }

    private void updateTimer() {
        if(textDisplay == null) return;
        if(timeoutTimer.getTimeLeftInSeconds() <= 0) {
            textDisplay.remove();
            textDisplay = null;
        } else {
            textDisplay.text(Component.text(String.format("%.1fs", timeoutTimer.getTimeLeftInSeconds()), NamedTextColor.GRAY));
        }
    }

    private Set<Vector> circlePoints() {
        if(circlePoints == null) {
            circlePoints = ShapeUtil.circle(getEntityCollisionRadius(), false, 32, false);
        }
        return circlePoints;
    }

    private Set<Vector> largeCirclePoints() {
        if(largeCirclePoints == null) {
            largeCirclePoints = ShapeUtil.circle(explosionRadius, false, 32, false);
        }
        return largeCirclePoints;
    }

    @Override
    public void onRapidUpdate() {

    }

    @Override
    public void onCollide(Entity entity) {
        if(!isActive) return;
        if(!dc.getTeamManager().areEntitiesOnDifferentTeams(entity, getOwner())) return;

        if(isTriggered) return;

        isTriggered = true;
        new Timer(dc.getPlugin(), triggerDelay, this::remove).start();
    }

    @Override
    public void onCollideWithBlock(Block block) {
        if(timer != null || isActive) return;

        timer = new Timer(dc.getPlugin(), activateDelay,
                () -> {
                    Location location = getItem().getLocation().clone().add(0, 0.5, 0);
                    getItem().getWorld().spawnParticle(
                            Particle.DUST,
                            dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner()),
                            null,
                            location.x(),
                            location.y(),
                            location.z(),
                            5,     // count
                            0.2,   // offsetX - random spread in X direction
                            0.2,   // offsetY - random spread in Y direction
                            0.2,   // offsetZ - random spread in Z direction
                            0.05,  // extra (speed) - gives particles initial velocity
                            new Particle.DustOptions(Color.FUCHSIA, 2)
                    );
                    isActive = true;
                    if(showRemainingDuration) {
                        textDisplay = getItem().getWorld().spawn(
                                getItem().getLocation().clone().add(0, 0.5, 0),
                                TextDisplay.class,
                                entity -> {
                                    entity.setBillboard(Display.Billboard.CENTER);
                                    entity.setVisibleByDefault(false);
                                    dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner()).forEach(player -> player.showEntity(dc.getPlugin(), entity));
                                }
                                );
                    }

                    if(true) {
                        getItem().setVisibleByDefault(false);
                        blockDisplay = getItem().getWorld().spawn(getItem().getLocation(),
                                BlockDisplay.class,
                                entity -> {
//                                    entity.setVisibleByDefault(false);
//                                    dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner()).forEach(player -> player.showEntity(dc.getPlugin(), entity));
                                    entity.setBlock(Material.SPORE_BLOSSOM.createBlockData());
                                     entity.setTransformationMatrix(
                                             new Matrix4f()
                                                     .scale(2) // scale up by a factor of 2 on all axes
                                                     .rotateXYZ(
                                                             (float) Math.toRadians(180), // rotate -45 degrees on the X axis
                                                             0,
                                                             0
                                                     )
                                     );
                                });
                    }

                    timeoutTimer.start();
                });
        timer.start();
    }

    @Override
    public void onDespawn() {

        timer.stop();
        timeoutTimer.stop();

        if(textDisplay != null) {
            textDisplay.remove();
            textDisplay = null;
        }

//        if(blockDisplay != null) {
//            blockDisplay.remove();
//            blockDisplay = null;
//        }

        if(!isTriggered) return;
        // Spawn explosion particles in a 2-block radius sphere
        Set<Vector> spherePoints = ShapeUtil.circle(getEntityCollisionRadius(), false, 64, false);

        spherePoints.forEach(point -> {
            getItem().getWorld().spawnParticle(
                    Particle.DUST,
                    getItem().getLocation().clone().add(point),
                    1,
                    0.1,   // Small offset for variation
                    0.1,
                    0.1,
                    0.5,   // Higher speed for explosive effect
                    new Particle.DustOptions(Color.FUCHSIA, 1.5f)
            );
        });

        Set<Vector> largeSpherePoints = ShapeUtil.circle(explosionRadius, false, 64, false);

        largeSpherePoints.forEach(point -> {
            getItem().getWorld().spawnParticle(
                    Particle.DUST,
                    getItem().getLocation().clone().add(point),
                    1,
                    0.1,   // Small offset for variation
                    0.1,
                    0.1,
                    0.5,   // Higher speed for explosive effect
                    new Particle.DustOptions(Color.FUCHSIA, 1.5f)
            );
        });

        // Optional: Add a burst effect at the center
        getItem().getWorld().spawnParticle(
                Particle.DUST,
                getItem().getLocation().clone().add(0, 0.5, 0),
                30,    // More particles for central burst
                0.3,   // Larger spread
                0.3,
                0.3,
                1.0,   // Fast speed for explosion effect
                new Particle.DustOptions(Color.FUCHSIA, 2f)
        );
        getItem().getWorld().spawnParticle(
                Particle.DUST,
                getItem().getLocation().clone().add(0, 0.5, 0),
                50,    // More particles for central burst
                1,   // Larger spread
                1,
                1,
                1.0,   // Fast speed for explosion effect
                new Particle.DustOptions(Color.FUCHSIA, 2f)
        );

        if(!(getOwner() instanceof LivingEntity ownerLiving)) return;

        getItem().getNearbyEntities(explosionRadius, explosionRadius, explosionRadius).stream()
                .filter((entity -> entity instanceof LivingEntity))
                .filter(livingEntity -> dc.getTeamManager().areEntitiesOnDifferentTeams(livingEntity, getOwner()))
                .forEach(
                        livingEntity -> {
                            CustomDamageEvent customDamageEvent = new CustomDamageEvent(
                                    dc,
                                    (LivingEntity) livingEntity,
                                    ownerLiving,
                                    null,
                                    explosionDamage,
                                    getItem().getLocation(),
                                    CustomDamageSource.SKILL,
                                    SkillId.LOTUS_TRAP.toString(),
                                    true
                            );

                            customDamageEvent.callEvent();

                            if(customDamageEvent.isCancelled()) return;

                            customDamageEvent.getCommand().execute();
                        }
                );
    }
}
