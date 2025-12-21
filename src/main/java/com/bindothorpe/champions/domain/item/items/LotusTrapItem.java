package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;

import java.util.Set;

/**
 * A trap item that activates after landing, triggers when enemies approach,
 * and explodes after a delay, dealing damage and applying slow effects.
 */
public class LotusTrapItem extends GameItem {

    // Constants
    private static final Color PARTICLE_COLOR = Color.FUCHSIA;
    private static final float PARTICLE_SIZE = 1f;
    private static final int CIRCLE_POINTS = 32;
    private static final int SPHERE_POINTS = 64;

    // State flags
    private boolean isTriggered = false;
    private boolean isActive = false;

    // Timers
    private Timer activationTimer = null;
    private final Timer timeoutTimer;

    // Configuration
    private final double activateDelay;
    private final double triggerDelay;
    private final double explosionRadius;
    private final double explosionDamage;
    private final double slowModifier;
    private final boolean showRemainingDuration;

    // Cached geometry
    private Set<Vector> circlePoints;
    private Set<Vector> largeCirclePoints;

    // Display entities
    private TextDisplay textDisplay;
    private BlockDisplay blockDisplay;
    private BlockDisplay blockDisplayLarge;

    // ==================== Constructors ====================

    public LotusTrapItem(DomainController dc, Player owner, double collisionRadius,
                         double activeDuration, double activateDelay, double triggerDelay,
                         double explosionRadius, double explosionDamage, double slowModifier,
                         boolean showRemainingDuration) {
        super(dc, Material.SPORE_BLOSSOM, -1, owner, collisionRadius, 0.15, BlockCollisionMode.TOP_ONLY);

        this.activateDelay = activateDelay;
        this.triggerDelay = triggerDelay;
        this.explosionRadius = explosionRadius;
        this.explosionDamage = explosionDamage;
        this.slowModifier = slowModifier;
        this.showRemainingDuration = showRemainingDuration;

        this.timeoutTimer = new Timer(dc.getPlugin(), activeDuration, this::remove);
    }

    public LotusTrapItem(DomainController dc, Player owner, double collisionRadius,
                         double activeDuration, double activateDelay, double triggerDelay,
                         double explosionRadius, double explosionDamage, double slowModifier) {
        this(dc, owner, collisionRadius, activeDuration, activateDelay, triggerDelay,
                explosionRadius, explosionDamage, slowModifier, false);
    }

    // ==================== Lifecycle Methods ====================

    @Override
    public void onTickUpdate() {
        if (!isActive) {
            handleTrail();
            return;
        }

        if (isTriggered) {
            spawnLargeCircleParticles();
        } else {
            spawnSmallCircleParticles();
            if (showRemainingDuration) {
                updateTimer();
            }
        }
    }

    @Override
    public void onRapidUpdate() {
        // No rapid update logic needed
    }

    @Override
    public void onCollide(Entity entity) {
        if (!isActive || isTriggered) return;
        if (!dc.getTeamManager().areEntitiesOnDifferentTeams(entity, getOwner())) return;

        isTriggered = true;
        makeVisibleToAll();
        new Timer(dc.getPlugin(), triggerDelay, this::remove).start();
    }

    @Override
    public void onCollideWithBlock(Block block) {
        if (activationTimer != null || isActive) return;

        activationTimer = new Timer(dc.getPlugin(), activateDelay, () -> {
            spawnFlower();
            timeoutTimer.start();
        });
        activationTimer.start();
    }

    @Override
    public void onDespawn() {
        cleanupTimers();
        cleanupDisplays();

        if (!isTriggered) {
            playBreakSound();
            return;
        }

        spawnExplosionParticles();
        damageNearbyEnemies();
        playExplosionSound();
    }

    // ==================== Particle Effects ====================

    private void handleTrail() {
        Location loc = getItem().getLocation();
        spawnParticleForTeam(loc, 1, 0.0, 0.0, 0.0, 0.0, PARTICLE_SIZE);
    }

    private void spawnSmallCircleParticles() {
        circlePoints().forEach(point -> {
            Location loc = getItem().getLocation().add(point);
            spawnParticleForTeam(loc, 1, 0.0, 0.0, 0.0, 0.0, PARTICLE_SIZE);
        });
    }

    private void spawnLargeCircleParticles() {
        largeCirclePoints().forEach(point -> {
            Location loc = getItem().getLocation().clone().add(point);
            // After triggered, particles are visible to everyone
            getItem().getWorld().spawnParticle(
                    Particle.DUST, loc, 1,
                    new Particle.DustOptions(PARTICLE_COLOR, PARTICLE_SIZE)
            );
        });

        playHissingSound();
        applySlowToNearbyEnemies();
    }

    private void spawnExplosionParticles() {
        Location center = getItem().getLocation();

        // Small sphere
        ShapeUtil.circle(getEntityCollisionRadius(), false, SPHERE_POINTS, false).forEach(point -> {
            spawnExplosionParticle(center.clone().add(point));
        });

        // Large sphere
        ShapeUtil.circle(explosionRadius, false, SPHERE_POINTS, false).forEach(point -> {
            spawnExplosionParticle(center.clone().add(point));
        });

        // Central burst effects
        Location burstLoc = center.clone().add(0, 0.5, 0);
        getItem().getWorld().spawnParticle(
                Particle.DUST, burstLoc, 30, 0.3, 0.3, 0.3, 1.0,
                new Particle.DustOptions(PARTICLE_COLOR, 2f)
        );
        getItem().getWorld().spawnParticle(
                Particle.DUST, burstLoc, 50, 1, 1, 1, 1.0,
                new Particle.DustOptions(PARTICLE_COLOR, 2f)
        );
    }

    private void spawnExplosionParticle(Location loc) {
        getItem().getWorld().spawnParticle(
                Particle.DUST, loc, 1, 0.1, 0.1, 0.1, 0.5,
                new Particle.DustOptions(PARTICLE_COLOR, 1.5f)
        );
    }

    private void spawnParticleForTeam(Location loc, int count, double offsetX,
                                      double offsetY, double offsetZ, double speed, float size) {
        getItem().getWorld().spawnParticle(
                Particle.DUST,
                dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner()),
                null,
                loc.x(), loc.y(), loc.z(),
                count, offsetX, offsetY, offsetZ, speed,
                new Particle.DustOptions(PARTICLE_COLOR, size)
        );
    }

    // ==================== Activation & Display ====================

    private void spawnFlower() {
        Location location = getItem().getLocation().clone().add(0, 0.5, 0);
        dc.getSoundManager().playSound(getItem().getLocation(), CustomSound.SKILL_LOTUS_TRAP_READY);
        spawnParticleForTeam(location, 5, 0.2, 0.2, 0.2, 0.05, 2f);

        isActive = true;

        createTextDisplay();
        hideItemAndShowBlock();
    }

    private void createTextDisplay() {
        if (!showRemainingDuration) return;

        textDisplay = getItem().getWorld().spawn(
                getItem().getLocation().clone().add(0, 0.5, 0),
                TextDisplay.class,
                entity -> {
                    entity.setBillboard(Display.Billboard.CENTER);
                    entity.setVisibleByDefault(false);
                    dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner())
                            .forEach(player -> player.showEntity(dc.getPlugin(), entity));
                }
        );
    }

    private void hideItemAndShowBlock() {
        getItem().setVisibleByDefault(false);

        blockDisplay = getItem().getWorld().spawn(
                getItem().getLocation(),
                BlockDisplay.class,
                entity -> {
                    entity.setBlock(Material.SPORE_BLOSSOM.createBlockData());
                    entity.setTransformationMatrix(
                            new Matrix4f()
                                    .scale(1f)
                                    .rotateXYZ(
                                            (float) Math.toRadians(180),
                                            (float) Math.toRadians(45),
                                            0
                                    )
                                    .translate(-0.5f, -1f, -0.5f)
                    );
                    // Make block display only visible to teammates until triggered
                    entity.setVisibleByDefault(false);
                    dc.getTeamManager().getPlayersOnTeamOfEntity(getOwner())
                            .forEach(player -> player.showEntity(dc.getPlugin(), entity));
                }
        );
    }

    private void makeVisibleToAll() {
        // Make block display visible to everyone when triggered
        if (blockDisplay != null) {
            blockDisplay.setVisibleByDefault(true);
        }

        // Make text display visible to everyone when triggered (if enabled)
        if (textDisplay != null) {
            textDisplay.setVisibleByDefault(true);
        }
    }

    private void updateTimer() {
        if (textDisplay == null) return;

        if (timeoutTimer.getTimeLeftInSeconds() <= 0) {
            textDisplay.remove();
            textDisplay = null;
        } else {
            textDisplay.text(Component.text(
                    String.format("%.1fs", timeoutTimer.getTimeLeftInSeconds()),
                    NamedTextColor.GRAY
            ));
        }
    }

    // ==================== Effects & Damage ====================

    private void playHissingSound() {
        dc.getSoundManager().playSound(getItem().getLocation(), CustomSound.SKILL_LOTUS_TRAP_HISS);
    }

    private void playExplosionSound() {
        dc.getSoundManager().playSound(getItem().getLocation(), CustomSound.SKILL_LOTUS_TRAP_EXPLODE);
    }

    private void applySlowToNearbyEnemies() {
        for (Entity entity : getItem().getNearbyEntities(explosionRadius, explosionRadius, explosionRadius)) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (!dc.getTeamManager().areEntitiesOnDifferentTeams(livingEntity, getOwner())) continue;

            dc.getEntityStatusManager().addEntityStatus(
                    livingEntity.getUniqueId(),
                    new EntityStatus(
                            EntityStatusType.MOVEMENT_SPEED,
                            -slowModifier,
                            triggerDelay,
                            false,
                            false,
                            this
                    )
            );
            dc.getEntityStatusManager().updateEntityStatus(
                    livingEntity.getUniqueId(),
                    EntityStatusType.MOVEMENT_SPEED
            );
        }
    }

    private void damageNearbyEnemies() {
        if (!(getOwner() instanceof LivingEntity ownerLiving)) return;

        getItem().getNearbyEntities(explosionRadius, explosionRadius, explosionRadius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> dc.getTeamManager().areEntitiesOnDifferentTeams(entity, getOwner()))
                .forEach(entity -> dealDamageTo((LivingEntity) entity, ownerLiving));
    }

    private void dealDamageTo(LivingEntity target, LivingEntity owner) {
        CustomDamageEvent customDamageEvent = CustomDamageEvent.getBuilder()
                .setDamager(owner)
                .setDamagee(target)
                .setDamage(explosionDamage)
                .setSendSkillHitToCaster(true)
                .setSendSkillHitToReceiver(true)
                .setCauseDisplayName(dc.getSkillManager().getSkillName(SkillId.LOTUS_TRAP))
                .setCause(CustomDamageEvent.DamageCause.SKILL)
                .build();

        customDamageEvent.callEvent();

        if (customDamageEvent.isCancelled()) return;

        new CustomDamageCommand(dc, customDamageEvent).execute();
    }

    // ==================== Cleanup ====================

    private void cleanupTimers() {
        if (activationTimer != null) {
            activationTimer.stop();
        }
        timeoutTimer.stop();
    }

    private void cleanupDisplays() {
        if (textDisplay != null) {
            textDisplay.remove();
            textDisplay = null;
        }

        if (blockDisplay != null) {
            blockDisplay.remove();
            blockDisplay = null;
        }

        if (blockDisplayLarge != null) {
            blockDisplayLarge.remove();
            blockDisplayLarge = null;
        }
    }

    private void playBreakSound() {
        dc.getSoundManager().playSound(getItem().getLocation(), CustomSound.SKILL_LOTUS_TRAP_TIMEOUT);
    }

    // ==================== Geometry Utilities ====================

    private Set<Vector> circlePoints() {
        if (circlePoints == null) {
            circlePoints = ShapeUtil.circle(getEntityCollisionRadius(), false, CIRCLE_POINTS, false);
        }
        return circlePoints;
    }

    private Set<Vector> largeCirclePoints() {
        if (largeCirclePoints == null) {
            largeCirclePoints = ShapeUtil.circle(explosionRadius, false, CIRCLE_POINTS, false);
        }
        return largeCirclePoints;
    }
}