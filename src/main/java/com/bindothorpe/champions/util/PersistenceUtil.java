package com.bindothorpe.champions.util;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PersistenceUtil {

    public static <P, C> void setData(PersistentDataHolder persistentDataHolder, NamespacedKey key, PersistentDataType<P, C> dataType, C value) {
        persistentDataHolder.getPersistentDataContainer().set(key, dataType, value);
    }

    public static void removeData(PersistentDataHolder persistentDataHolder, NamespacedKey key) {
        persistentDataHolder.getPersistentDataContainer().remove(key);
    }

    public static <P, C> C getData(PersistentDataHolder persistentDataHolder, NamespacedKey key, PersistentDataType<P, C> dataType) {
        return persistentDataHolder.getPersistentDataContainer().get(key, dataType);
    }

    public static <P, C> boolean hasData(PersistentDataHolder persistentDataHolder, NamespacedKey key, PersistentDataType<P, C> dataType) {
        return persistentDataHolder.getPersistentDataContainer().has(key, dataType);
    }

    public static void setDamageCauseForProjectile(@NotNull DomainController dc, @NotNull Projectile projectile, CustomDamageEvent.DamageCause damageCause, boolean overrideIfPresent) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "DamageCause");

        // If we should NOT override and data already exists, return early
        if (!overrideIfPresent && projectile.getPersistentDataContainer().has(key)) {
            dc.getPlugin().getLogger().warning("Data already exists: " + getDamageCauseOfProjectile(dc, projectile));
            return;
        }

        // Otherwise, add/override the data
        PersistenceUtil.setData(projectile, key, PersistentDataType.STRING, damageCause.toString());
    }

    public static void setDamageCauseForProjectile(@NotNull DomainController dc, @NotNull Projectile projectile, CustomDamageEvent.DamageCause damageCause) {
        setDamageCauseForProjectile(dc, projectile, damageCause, true);
    }

    public static @NotNull CustomDamageEvent.DamageCause getDamageCauseOfProjectile(@NotNull DomainController dc, @NotNull Projectile projectile) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "DamageCause");
        return projectile.getPersistentDataContainer().has(key) ? CustomDamageEvent.DamageCause.valueOf(projectile.getPersistentDataContainer().get(key, PersistentDataType.STRING)) : CustomDamageEvent.DamageCause.ATTACK_PROJECTILE;
    }

    public static void setSkillIdForProjectile(@NotNull DomainController dc, @NotNull Projectile projectile, @NotNull SkillId id) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "SkillId");
        setData(projectile, key, PersistentDataType.STRING, id.toString());
    }

    public static @Nullable SkillId getSkillIdOfProjectile(@NotNull DomainController dc, @NotNull Projectile projectile) {
        NamespacedKey key = new NamespacedKey(dc.getPlugin(), "SkillId");
        return projectile.getPersistentDataContainer().has(key) ? SkillId.valueOf(projectile.getPersistentDataContainer().get(key, PersistentDataType.STRING)) : null;
    }
}
