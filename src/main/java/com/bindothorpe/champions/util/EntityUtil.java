package com.bindothorpe.champions.util;

import com.bindothorpe.champions.events.interact.blocking.PlayerBlockListener;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
}
