package com.bindothorpe.champions.command.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.Command;
import com.bindothorpe.champions.command.death.CustomDeathCommand;
import com.bindothorpe.champions.domain.combat.DamageLog;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.death.CustomDeathEvent;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
    private boolean suppressHitSound = false;
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
        this(dc, event.getDamagee(), (LivingEntity) event.getDamager(), event.getOriginalDamage(), event.getAttackLocation(), event.getSource());
    }

    @Override
    public void execute() {
        if(hasExecuted) {
            throw new IllegalStateException("This command has already been executed");
        }


        if(damagee.isDead()) return;
        // Damage the entity
        double newHealth = damagee.getHealth() - (overwriteDamage == Double.MIN_VALUE ? getFinalDamage() : overwriteDamage);

        if(newHealth <= 0 && damagee instanceof Player) {
            handleCustomDeathEvent();
            return;
        }

        damagee.setHealth(newHealth > 0 ? newHealth : 0);

        damagee.setVelocity(getFinalKnockbackVector(damagee));


        if(getDamage() > 0) {
            // Play the damage animation
            damagee.playHurtAnimation(0);

            // Play the hurt sound
            if(damagee.getHurtSound() != null) damagee.getWorld().playSound(Sound.sound().type(damagee.getHurtSound()).build(), damagee);

        }

        hasExecuted = true;
    }

    private void handleCustomDeathEvent() {
        Player player = (Player) damagee;
        CustomDeathEvent customDeathEvent = new CustomDeathEvent(player);
        customDeathEvent.setDeathMessage(getCustomDeathMessage(dc, dc.getCombatLogger().getLastLog(player.getUniqueId())));
        if(player.getRespawnLocation() == null) customDeathEvent.setRespawnLocation(player.getWorld().getSpawnLocation());
        customDeathEvent.callEvent();
        System.out.println("Custom death event is called");

        if(customDeathEvent.isCancelled()) return;

        CustomDeathCommand customDeathCommand = new CustomDeathCommand(customDeathEvent);

        customDeathCommand.execute();
    }

    public double getDamage() {
        return overwriteDamage == Double.MIN_VALUE ? getFinalDamage() : overwriteDamage;
    }

    private Vector getFinalKnockbackVector(LivingEntity damagee) {
        if(overwriteDirection == null) {
            overwriteDirection = getKnockbackDirection();
        }

        double force = overwriteForce == Double.MIN_VALUE ? getFinalKnockback() : overwriteForce;

        // Separate horizontal and vertical components
        Vector horizontal = overwriteDirection.clone().setY(0).normalize();
        double verticalComponent = overwriteDirection.getY();

        // Apply force only to horizontal, keep vertical as-is
        Vector knockback = horizontal.multiply(force).setY(verticalComponent * (Math.min(ORIGINAL_KNOCKBACK, force)));

        // Add to existing velocity
        return damagee.getVelocity().add(knockback);
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

        EntityStatusType attackStatusTypeToCheck = EntityStatusType.ATTACK_KNOCKBACK_DONE;

        if(source.equals(CustomDamageSource.SKILL)) {
            attackStatusTypeToCheck = EntityStatusType.SKILL_KNOCKBACK_DONE;
        }
        if(source.equals(CustomDamageSource.ATTACK_PROJECTILE)) {
            attackStatusTypeToCheck = EntityStatusType.PROJECTILE_KNOCKBACK_DONE;
        }

        EntityStatusType receiveStatusTypeToCheck = EntityStatusType.ATTACK_KNOCKBACK_RECEIVED;

        if(source.equals(CustomDamageSource.SKILL)) {
            receiveStatusTypeToCheck = EntityStatusType.SKILL_KNOCKBACK_RECEIVED;
        }
        if(source.equals(CustomDamageSource.ATTACK_PROJECTILE)) {
            receiveStatusTypeToCheck = EntityStatusType.PROJECTILE_KNOCKBACK_RECEIVED;
        }

        return Math.max(0, calculateValue(attackStatusTypeToCheck,
                receiveStatusTypeToCheck,
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


    public Vector getDirection() {
        return damagee.getLocation().toVector().subtract(attackLocation.toVector()).normalize();
    }

    private Vector getKnockbackDirection() {
        return damagee.getLocation().toVector().subtract(attackLocation.toVector()).setY(0).normalize().setY(ORIGINAL_VERTICAL_KNOCKBACK).normalize();
    }

    public void suppressHitSound() {
        this.suppressHitSound = true;
    }

    public boolean shouldSuppressHitSound() {
        return suppressHitSound;
    }

    public static Component getCustomDeathMessage(DomainController dc, DamageLog damageLog) {

        Player player = Bukkit.getPlayer(damageLog.receiver());

        if(player == null) return null;

        if(damageLog.attacker() == null) {
            return Component.text(player.getName()).color(dc.getTeamManager().getTeamFromEntity(player).getTextColor())
                    .append(Component.text(" died.").color(NamedTextColor.GRAY));
        }

        Player attacker = Bukkit.getPlayer(damageLog.attacker());

        if(attacker == null) return null;

        Component message = Component.text(player.getName()).color(dc.getTeamManager().getTeamFromEntity(player).getTextColor())
                .append(Component.text(" was killed by ").color(NamedTextColor.GRAY))
                .append(Component.text(attacker.getName()).color(dc.getTeamManager().getTeamFromEntity(attacker).getTextColor()));



        if(damageLog.damageSourceString() == null) {
            message = message.append(Component.text(".").color(NamedTextColor.GRAY));
        } else {
            message = message.append(Component.text(" using ").color(NamedTextColor.GRAY)
                    .append(Component.text(damageLog.damageSourceString()).color(NamedTextColor.YELLOW))
                    .append(Component.text(".").color(NamedTextColor.GRAY)));
        }

        return message;
    }
}
