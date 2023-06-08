package com.bindothorpe.champions.events.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CustomDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private LivingEntity entity;
    private Entity hitBy;
    private double damage;
    private Set<DamageModification> damageMods;
    private Set<KnockbackModification> knockbackMods;
    private CustomDamageSource source;
    private boolean cancelled;

    public CustomDamageEvent(LivingEntity entity, Entity hitBy, double damage, Set<DamageModification> damageMods, Set<KnockbackModification> knockbackMods, CustomDamageSource source) {
        this.entity = entity;
        this.hitBy = hitBy;
        this.damage = damage;
        this.damageMods = damageMods;
        this.knockbackMods = knockbackMods;
        this.source = source;
        this.cancelled = false;
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

    public double getDamage() {
        return damage;
    }

    public Set<DamageModification> getDamageMods() {
        return damageMods;
    }

    public Set<KnockbackModification> getKnockbackMods() {
        return knockbackMods;
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
}
