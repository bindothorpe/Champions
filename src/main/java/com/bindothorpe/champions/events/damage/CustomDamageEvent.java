package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.effect.PlayerEffect;
import com.bindothorpe.champions.domain.effect.PlayerEffectType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class CustomDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final DomainController dc;
    private final LivingEntity entity;
    private final Entity hitBy;
    private final double originalDamage;
    private final static double ORIGINAL_KNOCBKAC = 0.6;
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
        return Math.max(0, calculateValue(PlayerEffectType.DAMAGE_DONE, PlayerEffectType.DAMAGE_RECEIVED, originalDamage));
    }

    public double getFinalKnockback() {
        return Math.max(0, calculateValue(PlayerEffectType.KNOCKBACK_DONE, PlayerEffectType.KNOCKBACK_RECEIVED, ORIGINAL_KNOCBKAC));
    }

    private double calculateValue(PlayerEffectType done, PlayerEffectType received, double originalValue) {

        Set<PlayerEffect> doneSet = new HashSet<>();
        Set<PlayerEffect> doneMultSet = new HashSet<>();

        doneSet.addAll(dc.getPlayerEffectsByType(hitBy.getUniqueId(), done, false));
        doneMultSet.addAll(dc.getPlayerEffectsByType(hitBy.getUniqueId(), done, true));


        Set<PlayerEffect> receivedSet = new HashSet<>();
        Set<PlayerEffect> receivedMultSet = new HashSet<>();

        receivedSet.addAll(dc.getPlayerEffectsByType(entity.getUniqueId(), received, false));
        receivedMultSet.addAll(dc.getPlayerEffectsByType(entity.getUniqueId(), received, true));

        double doneAddSum = doneSet.stream().reduce(0.0, (a, b) -> a + b.getValue(), Double::sum);
        double doneMultSum = doneMultSet.stream().reduce(1.0, (a, b) -> a + b.getValue(), Double::sum);

        double finalDone = doneAddSum * doneMultSum;

        double receivedAddSum = receivedSet.stream().reduce(0.0, (a, b) -> a + b.getValue(), Double::sum);
        double receivedMultSum = receivedMultSet.stream().reduce(1.0, (a, b) -> a + b.getValue(), Double::sum);

        double finalReceived = receivedAddSum * receivedMultSum;

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
