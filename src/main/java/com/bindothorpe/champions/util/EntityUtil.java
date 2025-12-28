package com.bindothorpe.champions.util;

import com.bindothorpe.champions.events.interact.blocking.PlayerBlockListener;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class EntityUtil {

    public static Location getCenteredLocation(LivingEntity livingEntity) {
        Location loc = livingEntity.getLocation();
        return loc.clone().add(0, livingEntity.getHeight() / 2, 0);
    }

    public static List<Entity> getCollidingEntities(LivingEntity livingEntity) {
        Location loc = getCenteredLocation(livingEntity);
        return livingEntity.getNearbyEntities(livingEntity.getWidth() / 2, livingEntity.getHeight() / 2, livingEntity.getWidth() / 2);
    }

    public static boolean isPlayerBlocking(@NotNull Player player) {
        return PlayerBlockListener.isPlayerBlocking(player);
    }

    public static double getPlayerBlockingDuration(@NotNull Player player) {
        return PlayerBlockListener.getPlayerBlockingDuration(player);
    }

    public static void fullyHealEntity(@NotNull LivingEntity entity) {
        try {
            entity.setHealth(Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).getValue());
        } catch (Exception ignored) {

        }
    }

    public static double getMaxHealth(@NotNull LivingEntity entity) {
        if(entity.getAttribute(Attribute.MAX_HEALTH) == null) return 20.0D;
        return entity.getAttribute(Attribute.MAX_HEALTH).getValue();
    }
}
