package com.bindothorpe.champions.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

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
}
