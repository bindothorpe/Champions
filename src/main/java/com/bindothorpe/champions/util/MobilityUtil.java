package com.bindothorpe.champions.util;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class MobilityUtil {

    public static void stopVelocity(final Entity entity) {
        stopVelocity(entity, true);
    }
    public static void stopVelocity(final Entity entity, boolean resetFallDistance) {
        if (resetFallDistance)
            entity.setFallDistance(0.0f);

        entity.setVelocity(new Vector(0, 0, 0));
    }

    public static void launch(final Entity entity, final double strength, final double yAdd, final double yMax, final boolean groundBoost) {
        launch(entity, entity.getLocation().getDirection(), strength, false, 0.0, yAdd, yMax, groundBoost);
    }

    public static void launch(final Entity entity, final Vector direction, final double strength, final boolean useYBase, final double yBase, final double yAdd, final double yMax, final boolean groundBoost) {
        //Check for NaN and zero length
        if (Double.isNaN(direction.getX()) || Double.isNaN(direction.getY()) || Double.isNaN(direction.getZ()) || direction.length() == 0.0) {
            return;
        }

        //Set Y to base value
        if (useYBase) {
            direction.setY(yBase);
        }
        //Normalize and multiply by strength
        direction.normalize().multiply(strength);

        //
        direction.setY(direction.getY() + yAdd);
        if (direction.getY() > yMax) {
            direction.setY(yMax);
        }
        //Add ground boost if the entity is on ground
        if (groundBoost && entity.isOnGround()) {
            direction.setY(direction.getY() + 0.2);
        }

        //Reset fall distance so the entity does not take damage
        entity.setFallDistance(0.0f);

        //Set the velocity
        entity.setVelocity(direction);
    }
}
