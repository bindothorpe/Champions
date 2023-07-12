package com.bindothorpe.champions.command.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.Command;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomDamageCommand implements Command {

    private final static double ORIGINAL_KNOCKBACK = 0.6;
    private final static double ORIGINAL_VERTICAL_KNOCKBACK = 0.9;
    private final DomainController dc;
    private final LivingEntity damagee;
    private final LivingEntity damager;
    private final double originalDamage;
    private final Location attackLocation;
    private final CustomDamageSource source;
    private Vector overwriteDirection;
    private double overwriteForce = Double.MIN_VALUE;
    private double overwriteDamage = Double.MIN_VALUE;
    private boolean hasExecuted = false;

    public CustomDamageCommand(DomainController dc, LivingEntity damagee, LivingEntity damager, double originalDamage, Location attackLocation, CustomDamageSource source) {
        this.dc = dc;
        this.damagee = damagee;
        this.damager = damager;
        this.originalDamage = originalDamage;
        this.attackLocation = attackLocation;
        this.source = source;
    }

    public CustomDamageCommand(DomainController dc, CustomDamageEvent event) {
        this(dc, event.getDamagee(), (LivingEntity) event.getDamager(), event.getOriginalDamage(), event.getDamager().getLocation(), event.getSource());
    }

    @Override
    public void execute() {
        if(hasExecuted) {
            throw new IllegalStateException("This command has already been executed");
        }

        // Damage the entity
        double newHealth = damagee.getHealth() - (overwriteDamage == Double.MIN_VALUE ? getFinalDamage() : overwriteDamage);
        damagee.setHealth(newHealth > 0 ? newHealth : 0);

        // Knock back the entity
        if(overwriteDirection == null) {
            overwriteDirection = getKnockbackDirection();
        }

        Vector newVelocity = damagee.getVelocity().add(overwriteDirection.multiply(overwriteForce == Double.MIN_VALUE ? getFinalKnockback() : overwriteForce));
        damagee.setVelocity(newVelocity);

        // Play the damage animation
        damagee.playEffect(EntityEffect.HURT);
        hasExecuted = true;
    }

    public double getDamage() {
        return overwriteDamage == Double.MIN_VALUE ? getFinalDamage() : overwriteDamage;
    }

    public double getForce() {
        return overwriteForce == Double.MIN_VALUE ? getFinalKnockback() : overwriteForce;
    }

    public CustomDamageCommand direction(Vector direction) {
        this.overwriteDirection = direction;
        return this;
    }

    public CustomDamageCommand damage(double damage) {
        this.overwriteDamage = damage;
        return this;
    }

    public CustomDamageCommand force(double force) {
        this.overwriteForce = force;
        return this;
    }

    private double getFinalDamage() {
        return Math.max(0, calculateValue(source.equals(CustomDamageSource.ATTACK) ? EntityStatusType.ATTACK_DAMAGE_DONE : EntityStatusType.SKILL_DAMAGE_DONE,
                source.equals(CustomDamageSource.ATTACK) ? EntityStatusType.ATTACK_DAMAGE_RECEIVED : EntityStatusType.SKILL_DAMAGE_RECEIVED,
                originalDamage));
    }


    private double getFinalKnockback() {
        return Math.max(0, calculateValue(source.equals(CustomDamageSource.ATTACK) ? EntityStatusType.ATTACK_KNOCKBACK_DONE : EntityStatusType.SKILL_KNOCKBACK_DONE,
                source.equals(CustomDamageSource.ATTACK) ? EntityStatusType.ATTACK_KNOCKBACK_RECEIVED : EntityStatusType.SKILL_KNOCKBACK_RECEIVED,
                ORIGINAL_KNOCKBACK));
    }

    private double calculateValue(EntityStatusType done, EntityStatusType received, double originalValue) {

        double finalDone, finalReceived = 0;

        double doneMod = dc.getEntityStatusManager().getModifcationValue(damager.getUniqueId(), done);
        double doneMult = dc.getEntityStatusManager().getMultiplicationValue(damager.getUniqueId(), done);

        double receivedMod = dc.getEntityStatusManager().getModifcationValue(damagee.getUniqueId(), received);
        double receivedMult = dc.getEntityStatusManager().getMultiplicationValue(damagee.getUniqueId(), received);

        if (doneMult == 0 || receivedMult == 0) {
            return 0;
        }

        finalDone = (originalValue + doneMod) * doneMult;
        finalReceived = (originalValue + receivedMod) * receivedMult;


        return Math.max(0, originalValue + finalDone - finalReceived);
    }


    private final Vector getKnockbackDirection() {
        return damagee.getLocation().toVector().subtract(attackLocation.toVector()).setY(0).normalize().setY(ORIGINAL_VERTICAL_KNOCKBACK).normalize();
    }
}
