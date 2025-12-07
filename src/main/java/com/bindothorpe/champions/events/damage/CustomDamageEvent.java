package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.combat.DamageLog;
import com.bindothorpe.champions.domain.player.PlayerData;
import com.bindothorpe.champions.domain.skill.SkillId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CustomDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final DomainController dc;
    private final LivingEntity damagee;
    private final LivingEntity damager;
    private final double originalDamage;
    private Location attackLocation;
    private final CustomDamageSource source;
    private final String damageSourceString;
    private boolean cancelled;
    private CustomDamageCommand command;

    public CustomDamageEvent(DomainController dc, LivingEntity entity, LivingEntity hitBy, double originalDamage, Location attackLocation, CustomDamageSource source, String damageSourceString) {
        this.dc = dc;
        this.damagee = entity;
        this.damager = hitBy;
        this.originalDamage = originalDamage;
        this.attackLocation = attackLocation;
        this.source = source;
        this.damageSourceString = damageSourceString;
        this.cancelled = false;
    }

    public CustomDamageCommand getCommand() {
        return command;
    }

    public void setCommand(CustomDamageCommand command) {
        this.command = command;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public LivingEntity getDamagee() {
        return damagee;
    }

    public Entity getDamager() {
        return damager;
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

    public String getDamageSourceString() {
        return damageSourceString;
    }
}
