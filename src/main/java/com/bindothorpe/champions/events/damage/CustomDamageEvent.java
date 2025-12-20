package com.bindothorpe.champions.events.damage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CustomDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final DomainController dc;
    private final LivingEntity damagee;
    private final LivingEntity damager;
    private final Projectile projectile;
    private final double originalDamage;
    private Location attackLocation;
    private final CustomDamageSource source;
    private final String damageSourceString;
    private boolean cancelled;
    private CustomDamageCommand command;
    private boolean sendSkillHitToCaster = true;
    private boolean sendSkillHitToReceiver = true;

    public CustomDamageEvent(DomainController dc, LivingEntity entity, LivingEntity hitBy, Projectile projectile, double originalDamage, Location attackLocation, CustomDamageSource source, String damageSourceString, boolean createCommand) {
        this.dc = dc;
        this.damagee = entity;
        this.damager = hitBy;
        this.projectile = projectile;
        this.originalDamage = originalDamage;
        this.attackLocation = attackLocation;
        this.source = source;
        this.damageSourceString = damageSourceString;
        this.cancelled = false;
        if(createCommand) {
            this.command = new CustomDamageCommand(this.dc, this);
        }
    }

    public CustomDamageEvent(DomainController dc, LivingEntity entity, LivingEntity hitBy, Projectile projectile, double originalDamage, Location attackLocation, CustomDamageSource source, String damageSourceString) {
        this(dc, entity, hitBy, projectile, originalDamage, attackLocation, source, damageSourceString, false);
    }

    public CustomDamageEvent(DomainController dc, LivingEntity entity, LivingEntity hitBy, double originalDamage, Location attackLocation, CustomDamageSource source, String damageSourceString) {
        this(dc, entity, hitBy, null, originalDamage, attackLocation, source, damageSourceString);
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

    public Projectile getProjectile() {
        return projectile;
    }

    public static void addSkillIdData(@NotNull DomainController dc, @NotNull Projectile projectile, @NotNull SkillId skillId) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "skill_id");
        addCustomData(projectile, key, PersistentDataType.STRING, skillId.toString());
    }

    public static SkillId getSkillIdData(@NotNull DomainController dc, @NotNull Projectile projectile) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "skill_id");
        if(!projectile.getPersistentDataContainer().has(key)) return null;

        return SkillId.valueOf(projectile.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    public static void addCustomDamageSourceData(@NotNull DomainController dc, @NotNull Projectile projectile, @NotNull CustomDamageSource customDamageSource, boolean overrideIfPresent) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "custom_damage_source");

        // If we should NOT override and data already exists, return early
        if (!overrideIfPresent && hasCustomDamageSourceData(dc, projectile)) {
            System.out.println("Data already exists: " + getCustomDamageSourceData(dc, projectile));
            return;
        }

        // Otherwise, add/override the data
        addCustomData(projectile, key, PersistentDataType.STRING, customDamageSource.toString());
    }

    public static void addCustomDamageSourceData(@NotNull DomainController dc, @NotNull Projectile projectile, @NotNull CustomDamageSource customDamageSource) {
        addCustomDamageSourceData(dc, projectile, customDamageSource, true);
    }

    public static CustomDamageSource getCustomDamageSourceData(DomainController dc, Projectile projectile) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "custom_damage_source");
        if(!projectile.getPersistentDataContainer().has(key)) return null;

        return CustomDamageSource.valueOf(projectile.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    public static boolean hasSkillIdData(DomainController dc, Projectile projectile) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "skill_id");
        return projectile.getPersistentDataContainer().has(key);
    }


    public static boolean hasCustomDamageSourceData(DomainController dc, Projectile projectile) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "custom_damage_source");
        return projectile.getPersistentDataContainer().has(key);
    }

    private static <P, C> void addCustomData(Projectile projectile, NamespacedKey key, PersistentDataType<P, C> dataType, C value) {
        projectile.getPersistentDataContainer().set(key, dataType, value);
    }

    public Location getAttackLocation() {
        return attackLocation;
    }

    public void sendSkillHitToCaster(boolean sendSkillHitToCaster) {
        this.sendSkillHitToCaster = sendSkillHitToCaster;
    }

    public boolean doSendSkillHitToCaster() {
        return this.sendSkillHitToCaster;
    }

    public void sendSkillHitToReceiver(boolean sendSkillHitToReceiver) {
        this.sendSkillHitToReceiver = sendSkillHitToReceiver;
    }

    public boolean doSendSkillHitToReceiver() {
        return this.sendSkillHitToReceiver;
    }
}
