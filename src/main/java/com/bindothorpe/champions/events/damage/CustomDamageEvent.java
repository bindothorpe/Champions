package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomDamageEvent extends Event implements Cancellable {


    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;


    private final @Nullable LivingEntity damager;
    private final @NotNull LivingEntity damagee;
    private double damage;
    private double forceMultiplier = 1.0D;
    private final @NotNull DamageCause cause;
    private @Nullable String causeDisplayName;
    private final @Nullable Projectile projectile;
    private final @Nullable Location location;
    private @Nullable Vector direction;
    private boolean sendSkillHitToCaster;
    private boolean sendSkillHitToReceiver;
    private boolean suppressHitSound = false;
    private boolean horizontalKnockback = true;
    private boolean playDamageEffectAndSound = true;

    CustomDamageEvent(@Nullable LivingEntity damager, @NotNull LivingEntity damagee, double damage, double forceMultiplier, boolean horizontalKnockback, @NotNull DamageCause cause, @Nullable String causeDisplayName, @Nullable Projectile projectile, @Nullable Location location, Vector direction, boolean sendSkillHitToCaster, boolean sendSkillHitToReceiver, boolean playDamageEffectAndSound) {
        this.damager = damager;
        this.damagee = damagee;
        this.damage = damage;
        this.forceMultiplier = forceMultiplier;
        this.cause = cause;
        this.causeDisplayName = causeDisplayName;
        this.projectile = projectile;
        this.location = location;
        this.sendSkillHitToCaster = sendSkillHitToCaster;
        this.sendSkillHitToReceiver = sendSkillHitToReceiver;
        this.horizontalKnockback = horizontalKnockback;
        this.direction = direction;
        this.playDamageEffectAndSound = playDamageEffectAndSound;
    }


    // Static methods ------------------------------------------

    /**
     * Returns the builder to create this event class.
     *<p>
     * Possible values you can set using the builder are: <p>
     * {@link CustomDamageEventBuilder#setDamager(LivingEntity)},<p>
     * {@link CustomDamageEventBuilder#setDamagee(LivingEntity)}, <p>
     * {@link CustomDamageEventBuilder#setDamage(double)}, <p>
     * {@link CustomDamageEventBuilder#setCause(DamageCause)} (The damage cause that is used internally for calculating the final damage), <p>
     * {@link CustomDamageEventBuilder#setCauseDisplayName(String)} (The label displayed in chat when you kill, or hit someone with a skill), <p>
     * {@link CustomDamageEventBuilder#setProjectile(Projectile)} (If the event was caused by a projectile colliding with an entity), <p>
     * {@link CustomDamageEventBuilder#setLocation(Location)} (The location from where the knockback direction will be calculated), <p>
     * {@link CustomDamageEventBuilder#setSendSkillHitToCaster(boolean)} and {@link CustomDamageEventBuilder#setSendSkillHitToReceiver(boolean)} are used to send the skill damage to be printed to the sender and/or the receiver.
     *
     * @return CustomDamageEventBuilder to construct the event
     */
    public static CustomDamageEventBuilder getBuilder() {
        return new CustomDamageEventBuilder();
    }

    // -----------------------------------------------------------


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public @Nullable LivingEntity getDamager() {
        return damager;
    }

    public @NotNull LivingEntity getDamagee() {
        return damagee;
    }

    public double getDamage() {
        return damage;
    }

    /**
     * Sets the damage of the event.
     * <p>
     * Also check out {@link #modifyDamage(double)} to modify the current damage.
     * @param damage The value that should be set
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    /**
     * Modifies the damage of the event by the given number.
     * <p>
     * If the value is positive, it will add it to the current damage, if the value is
     * negative it will be subtracted from the current damage.
     * @param damageModifier Modifier of the current damage
     */
    public void modifyDamage(double damageModifier) {
        damage += damageModifier;
    }

    /**
     * Modifies the knockback force of the event by the given number.
     * <p>
     * If the value is positive, it will add it to the current force multiplier, if the value is
     * negative it will be subtracted from the current force multiplier.
     * <p>
     * The base value of the force multiplier is <code>1.0</code>.
     * <p>
     * This will not override {@link EntityStatusType#KNOCKBACK_DONE} or {@link EntityStatusType#KNOCKBACK_RECEIVED} modifiers, but will only change the base value for this event.
     * @param forceMultiplier Modifier of the current knockback force
     */
    public void modifyForce(double forceMultiplier) {
        this.forceMultiplier += forceMultiplier;
    }

    public double getForceMultiplier() {
        return forceMultiplier;
    }

    /**
     * Sets the force multiplier of the event.
     * <p>
     * Also check out {@link #modifyForce(double)} to modify the current force.
     * @param forceMultiplier The value that should be set
     */
    public void setForceMultiplier(double forceMultiplier) {
        this.forceMultiplier = forceMultiplier;
    }

    public void sendSkillHitToCaster(boolean sendSkillHitToCaster) {
        this.sendSkillHitToCaster = sendSkillHitToCaster;
    }

    public boolean sendSkillHitToCaster() {
        return sendSkillHitToCaster;
    }

    public void sendSkillHitToReceiver(boolean sendSkillHitToReceiver) {
        this.sendSkillHitToReceiver = sendSkillHitToReceiver;
    }

    public boolean sendSkillHitToReceiver() {
        return sendSkillHitToReceiver;
    }

    public @NotNull DamageCause getCause() {
        return cause;
    }

    public @Nullable String getCauseDisplayName() {
        return causeDisplayName;
    }

    public void setCauseDisplayName(@Nullable String causeDisplayName) {
        this.causeDisplayName = causeDisplayName;
    }

    public @Nullable Projectile getProjectile() {
        return projectile;
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public boolean doSuppressHitSound() {
        return this.suppressHitSound;
    }

    public void suppressHitSound(boolean suppressHitSound) {
        this.suppressHitSound = suppressHitSound;
    }

    public void setHorizontalKnockback(boolean horizontalKnockback) {
        this.horizontalKnockback = horizontalKnockback;
    }

    public boolean doHorizontalKnockback() {
        return this.horizontalKnockback;
    }

    public @Nullable Vector getDirection() {
        return direction;
    }

    public void setDirection(@Nullable Vector direction) {
        this.direction = direction;
    }

    public void playDamageEffectAndSound(boolean playDamageEffectAndSound) {
        this.playDamageEffectAndSound = playDamageEffectAndSound;
    }

    public boolean playDamageEffectAndSound() {
        return this.playDamageEffectAndSound;
    }


    public enum DamageCause {
        ATTACK,
        ATTACK_PROJECTILE,
        SKILL,
        SKILL_PROJECTILE,

        FALL,
        FIRE,
        FIRE_TICK,
        OTHER,
        ;

        public static DamageCause getValueOf(EntityDamageEvent.@NotNull DamageCause cause) {
            switch (cause) {
                case FALL -> {
                    return FALL;
                }
                case FIRE -> {
                    return FIRE;
                }
                case FIRE_TICK ->
                {
                    return FIRE_TICK;
                }
                default -> {
                    return OTHER;
                }
            }
        }
    }

}
