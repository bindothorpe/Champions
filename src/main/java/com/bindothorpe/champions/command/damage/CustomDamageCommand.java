package com.bindothorpe.champions.command.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.Command;
import com.bindothorpe.champions.command.death.CustomDeathCommand;
import com.bindothorpe.champions.domain.combat.DamageLog;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.death.CustomDeathEvent;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CustomDamageCommand implements Command {

    private final static double MINECRAFT_KNOCKBACK_STRENGTH = 0.6;
    private final static double MINECRAFT_VERTICAL_KNOCKBACK = 0.9;

    private final DomainController dc;
    private final CustomDamageEvent event;
    private boolean hasExecuted = false;

    public CustomDamageCommand(DomainController dc, CustomDamageEvent event) {
        this.dc = dc;
        this.event = event;
    }

    @Override
    public void execute() {
        if(hasExecuted) {
            throw new IllegalStateException("This command has already been executed");
        }

        LivingEntity damagee = event.getDamagee();

        if(damagee.isDead()) return;

        double healthAfterDamage = damagee.getHealth() - (getCalculatedDamage());
        System.out.println(healthAfterDamage);

        if(healthAfterDamage <= 0 && damagee instanceof Player) {
            createAndCallCustomDeathEvent();
            return;
        }


        // Deal damage and apply velocity
        damagee.setHealth(Math.clamp(healthAfterDamage, 0, damagee.getAttribute(Attribute.MAX_HEALTH) == null ? 20 : damagee.getAttribute(Attribute.MAX_HEALTH).getValue()));
        if(event.getLocation() != null)
            damagee.setVelocity(getCalculatedVelocity());

        // Play hurt animation and sound
        if(event.getDamage() > 0) {
            damagee.playHurtAnimation(0);

            if(damagee.getHurtSound() != null)
                damagee.getWorld().playSound(Sound.sound().type(damagee.getHurtSound()).build(), damagee);
        }

        // Flag the command as executed to prevent firing multiple times.
        hasExecuted = true;
    }


    private double getCalculatedDamage() {
        //TODO: Calculate the damage
        return event.getDamage();
//        return Math.max(0,
//                calculateEntityStatusValue(
//                    event.getCause().equals(CustomDamageEvent.DamageCause.ATTACK) ? EntityStatusType.ATTACK_DAMAGE_DONE : EntityStatusType.SKILL_DAMAGE_DONE,
//                    event.getCause().equals(CustomDamageEvent.DamageCause.ATTACK) ? EntityStatusType.ATTACK_DAMAGE_RECEIVED : EntityStatusType.SKILL_DAMAGE_RECEIVED,
//                    event.getDamage()
//                )
//        );
    }

    private @NotNull Vector getCalculatedVelocity() {
        Vector direction = getCalculatedKnockbackDirection();
        double force = getCalculatedKnockbackForce();

        double verticalLength = direction.getY();
        Vector horizontal = direction
                .clone()
                .setY(0)
                .normalize();

        Vector knockbackVelocity = event.doHorizontalKnockback() ? horizontal
                .multiply(force)
                .setY(verticalLength * (Math.min(MINECRAFT_VERTICAL_KNOCKBACK, force))) : // We use Math.min here because we don't want to go over the minecraft vertical knockback for normal attacks.
                direction.clone().multiply(force); // Here we use the force instead because we don't need to limit it.

        return event.getDamagee().getVelocity().add(knockbackVelocity);
    }

    private Vector getCalculatedKnockbackDirection() {
        if(event.getDirection() != null) {
            return event.getDirection();
        }

        if(event.getLocation() != null) {
            return event.doHorizontalKnockback() ? event.getDamagee().getLocation().toVector()
                    .subtract(event.getLocation().toVector())
                    .setY(0).normalize()
                    .setY(MINECRAFT_VERTICAL_KNOCKBACK).normalize() :
                    event.getDamagee().getLocation().toVector().subtract(event.getLocation().toVector());
        }

        return new Vector(0, 0, 0);
    }

    private double getCalculatedKnockbackForce() {
        EntityStatusType attackStatusTypeToCheck = EntityStatusType.ATTACK_KNOCKBACK_DONE;

        CustomDamageEvent.DamageCause damageCause = event.getCause();

        if(damageCause.equals(CustomDamageEvent.DamageCause.SKILL)) {
            attackStatusTypeToCheck = EntityStatusType.SKILL_KNOCKBACK_DONE;
        }
        if(damageCause.equals(CustomDamageEvent.DamageCause.ATTACK_PROJECTILE)) {
            attackStatusTypeToCheck = EntityStatusType.PROJECTILE_KNOCKBACK_DONE;
        }

        EntityStatusType receiveStatusTypeToCheck = EntityStatusType.ATTACK_KNOCKBACK_RECEIVED;

        if(damageCause.equals(CustomDamageEvent.DamageCause.SKILL)) {
            receiveStatusTypeToCheck = EntityStatusType.SKILL_KNOCKBACK_RECEIVED;
        }
        if(damageCause.equals(CustomDamageEvent.DamageCause.ATTACK_PROJECTILE)) {
            receiveStatusTypeToCheck = EntityStatusType.PROJECTILE_KNOCKBACK_RECEIVED;
        }

        return Math.max(0, calculateEntityStatusValue(
                attackStatusTypeToCheck,
                receiveStatusTypeToCheck,
                MINECRAFT_KNOCKBACK_STRENGTH * event.getForceMultiplier()));
    }

    private double calculateEntityStatusValue(EntityStatusType done, EntityStatusType received, double originalValue) {

        LivingEntity damager = event.getDamager();
        LivingEntity damagee = event.getDamagee();

        double finalDone, finalReceived;

        double doneMod = damager == null ? 0 : dc.getEntityStatusManager().getModifcationValue(damager.getUniqueId(), done);
        double doneMult = damager == null ? 0 : dc.getEntityStatusManager().getMultiplicationValue(damager.getUniqueId(), done);

        double receivedMod = dc.getEntityStatusManager().getModifcationValue(damagee.getUniqueId(), received);
        double receivedMult = dc.getEntityStatusManager().getMultiplicationValue(damagee.getUniqueId(), received);

        if (doneMult == 0 || receivedMult == 0) {
            return 0;
        }

        finalDone = (originalValue + doneMod) * doneMult;
        finalReceived = (originalValue + receivedMod) * receivedMult;


        return Math.max(0, originalValue + finalDone - finalReceived);
    }

    private void createAndCallCustomDeathEvent() {
        Player player = (Player) event.getDamagee();
        CustomDeathEvent customDeathEvent = new CustomDeathEvent(player);

        customDeathEvent.setDeathMessage(getCustomDeathMessage(dc, dc.getCombatLogger().getLastLog(player.getUniqueId())));

        if(player.getRespawnLocation() == null)
            customDeathEvent.setRespawnLocation(player.getWorld().getSpawnLocation());

        customDeathEvent.callEvent();

        if(customDeathEvent.isCancelled()) return;

        CustomDeathCommand customDeathCommand = new CustomDeathCommand(customDeathEvent);
        customDeathCommand.execute();
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
