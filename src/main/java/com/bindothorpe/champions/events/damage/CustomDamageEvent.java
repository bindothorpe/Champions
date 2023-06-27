package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CustomDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final DomainController dc;
    private final LivingEntity entity;
    private final Entity hitBy;
    private final double originalDamage;
    private final static double ORIGINAL_KNOCKBACK = 0.6;
    private final static double ORIGINAL_VERTICAL_KNOCKBACK = 0.9;
    private Location attackLocation;
    private CustomDamageSource source;
    private boolean cancelled;

    public CustomDamageEvent(DomainController dc, LivingEntity entity, Entity hitBy, double originalDamage, Location attackLocation, CustomDamageSource source) {
        this.dc = dc;
        this.entity = entity;
        this.hitBy = hitBy;
        this.originalDamage = originalDamage;
        this.attackLocation = attackLocation;
        this.source = source;
        this.cancelled = false;
    }

    public double getFinalDamage() {
        return Math.max(0, calculateValue(EntityStatusType.DAMAGE_DONE, EntityStatusType.DAMAGE_RECEIVED, originalDamage));
    }

    public double getFinalKnockback() {
        return Math.max(0, calculateValue(EntityStatusType.KNOCKBACK_DONE, EntityStatusType.KNOCKBACK_RECEIVED, ORIGINAL_KNOCKBACK));
    }

    private double calculateValue(EntityStatusType done, EntityStatusType received, double originalValue) {

        double finalDone, finalReceived = 0;

        double doneMod = dc.getModificationEntityStatusValue(hitBy.getUniqueId(), done);
        double doneMult = dc.getMultiplicationEntityStatusValue(hitBy.getUniqueId(), done);

        double receivedMod = dc.getModificationEntityStatusValue(entity.getUniqueId(), received);
        double receivedMult = dc.getMultiplicationEntityStatusValue(entity.getUniqueId(), received);

        if (doneMult == 0 || receivedMult == 0) {
            return 0;
        }

        finalDone = (originalValue + doneMod) * doneMult;
        finalReceived = (originalValue + receivedMod) * receivedMult;


        return Math.max(0, originalValue + finalDone - finalReceived);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public Entity getHitBy() {
        return hitBy;
    }

    public final double getOriginalDamage() {
        return originalDamage;
    }

    public CustomDamageSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public final Vector getKnockbackDirection() {
        return entity.getLocation().toVector().subtract(attackLocation.toVector()).setY(0).normalize().setY(ORIGINAL_VERTICAL_KNOCKBACK).normalize();
    }

}
