package com.bindothorpe.champions.events.damage;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class CustomDamageEventBuilder {

    private LivingEntity damager;
    private LivingEntity damagee;
    private double damage;
    private double forceMultiplier = 1;
    private boolean horizontalKnockback = true;
    private CustomDamageEvent.DamageCause cause;
    private String causeDisplayName;
    private Projectile projectile;
    private Location location;
    private Vector direction;
    private boolean sendSkillHitToCaster = false;
    private boolean sendSkillHitToReceiver = false;

    CustomDamageEventBuilder() {}

    public CustomDamageEvent build() {
        assert damagee != null : "Damagee is null";
        assert cause != null : "Cause is null";
        return new CustomDamageEvent(damager, damagee, damage, forceMultiplier, horizontalKnockback, cause, causeDisplayName, projectile, location, direction, sendSkillHitToCaster, sendSkillHitToReceiver);
    }

    public CustomDamageEventBuilder setDamager(LivingEntity damager) {
        this.damager = damager;
        return this;
    }

    public CustomDamageEventBuilder setDamagee(LivingEntity damagee) {
        this.damagee = damagee;
        return this;
    }

    public CustomDamageEventBuilder setDamage(double damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the force multiplier for the knockback of this event.
     * @param forceMultiplier The force multiplier (default value = <code>1.0D</code>)
     * @return CustomDamageEventBuilder
     */
    public CustomDamageEventBuilder setForceMultiplier(double forceMultiplier) {
        this.forceMultiplier = forceMultiplier;
        return this;
    }

    public CustomDamageEventBuilder setCause(CustomDamageEvent.DamageCause cause) {
        this.cause = cause;
        return this;
    }

    public CustomDamageEventBuilder setCauseDisplayName(String causeDisplayName) {
        this.causeDisplayName = causeDisplayName;
        return this;
    }

    public CustomDamageEventBuilder setProjectile(Projectile projectile) {
        this.projectile = projectile;
        return this;
    }

    /**
     * Location from where the knockback should be dealt.
     * <p>
     * If location and direction are null, no knockback will be dealt.
     * <p>
     * For direction see {@link CustomDamageEventBuilder#setDirection(Vector)}.
     * @param location The location from where the knockback should be dealt
     */
    public CustomDamageEventBuilder setLocation(@Nullable Location location) {
        this.location = location;
        return this;
    }

    /**
     * Direction in which the knockback should be dealt.
     * <p>
     * If direction and location are null, no knockback will be dealt.
     * <p>
     * For location see {@link CustomDamageEventBuilder#setLocation(Location)}.
     * @param direction The direction in which the knockback should be dealt
     */
    public CustomDamageEventBuilder setDirection(@Nullable Vector direction) {
        this.direction = direction;
        return this;
    }

    public CustomDamageEventBuilder setSendSkillHitToCaster(boolean sendSkillHitToCaster) {
        this.sendSkillHitToCaster = sendSkillHitToCaster;
        return this;
    }

    public CustomDamageEventBuilder setSendSkillHitToReceiver(boolean sendSkillHitToReceiver) {
        this.sendSkillHitToReceiver = sendSkillHitToReceiver;
        return this;
    }

    /**
     * If set to <code>true</code>, the knockback mimic the default hit knockback of minecraft and will not launch the entity upwards.
     * If set to <code>false</code>, the entity will be able to be launched upwards exceeding the minecraft upwards knockback from default hits.
     * <p>
     * The value is <code>true</code> by default.
     * @param horizontalKnockback Whether the entity will be launched upwards exceeding minecraft's default hit limits.
     * @return CustomDamageEventBuilder
     */
    public CustomDamageEventBuilder setHorizontalKnockback(boolean horizontalKnockback) {
        this.horizontalKnockback = horizontalKnockback;
        return this;
    }
}
