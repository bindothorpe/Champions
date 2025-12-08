package com.bindothorpe.champions.util.raycast;

import com.bindothorpe.champions.util.raycast.RaycastResult;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RaycastUtil {

    /**
     * Performs a raycast from the player's eye location in their looking direction.
     * The raycast can detect both living entities and blocks along its path.
     *
     * @param player The player from whose eye location the raycast originates
     * @param distance The maximum distance the raycast will travel in blocks
     * @param densityPerBlock The number of sample points per block (e.g., 2.0 = 2 points per block).
     *                        Higher values provide more accurate detection but are more performance intensive
     * @param detectionRadius The radius around each raycast point to check for living entities.
     *                        Larger values make it easier to hit entities but less precise
     * @param passTroughTerrain If false, the raycast stops when hitting a non-passable block.
     *                          If true, the raycast ignores terrain and continues through blocks
     * @param passTroughEntity If false, the raycast stops when hitting any living entities.
     *                         If true, the raycast continues through entities along the entire path
     * @param allowMultipleEntitiesHit If true and passTroughEntity is false, collects all entities at the hit point.
     *                                 If true and passTroughEntity is true, collects all entities along the path.
     *                                 If false, only records the first entity encountered
     * @return A RaycastResult containing all living entities hit,
     *         the first block hit (if any), and all raycast points generated
     */
    public static RaycastResult drawRaycastFromPlayerInLookingDirection(
            Player player,
            double distance,
            double densityPerBlock,
            double detectionRadius,
            boolean passTroughTerrain,
            boolean passTroughEntity,
            boolean allowMultipleEntitiesHit) {

        List<Vector> points = new ArrayList<>();
        Set<LivingEntity> hitEntities = new HashSet<>();
        Block hitBlock = null;

        // Get player's eye location and direction
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        // Calculate step size based on density
        double stepSize = 1.0 / densityPerBlock;

        // Calculate total number of steps
        int steps = (int) Math.ceil(distance / stepSize);

        // Start from player's eye location
        Vector currentPoint = eyeLocation.toVector();

        for (int i = 0; i <= steps; i++) {
            // Add current point to the list
            points.add(currentPoint.clone());

            // Check for living entity hits
            Location checkLocation = currentPoint.toLocation(eyeLocation.getWorld());
            Set<LivingEntity> entitiesAtThisPoint = new HashSet<>();

            for (LivingEntity entity : eyeLocation.getWorld().getNearbyLivingEntities(checkLocation, detectionRadius)) {
                // Skip the player themselves
                if (entity.equals(player)) {
                    continue;
                }

                // Skip entities we've already hit (only relevant when passing through entities)
                if (hitEntities.contains(entity)) {
                    continue;
                }

                // Entity is already within detection radius from getNearbyLivingEntities
                entitiesAtThisPoint.add(entity);
            }

            // Handle entity hits based on settings
            if (!entitiesAtThisPoint.isEmpty()) {
                if (allowMultipleEntitiesHit) {
                    // Add all entities at this point
                    hitEntities.addAll(entitiesAtThisPoint);
                } else {
                    // Add only the first entity
                    hitEntities.add(entitiesAtThisPoint.iterator().next());
                }

                // Stop if we don't pass through entities
                if (!passTroughEntity) {
                    // Check for block hit at this final point before stopping
                    if (hitBlock == null && !passTroughTerrain) {
                        Block block = eyeLocation.getWorld().getBlockAt(
                                currentPoint.getBlockX(),
                                currentPoint.getBlockY(),
                                currentPoint.getBlockZ()
                        );
                        if (!block.isPassable()) {
                            hitBlock = block;
                        }
                    }
                    break;
                }
            }

            // Check for block hits (if we haven't hit one yet and terrain checking is enabled)
            if (hitBlock == null && !passTroughTerrain) {
                Block block = eyeLocation.getWorld().getBlockAt(
                        currentPoint.getBlockX(),
                        currentPoint.getBlockY(),
                        currentPoint.getBlockZ()
                );

                // Check if we hit a non-passable block
                if (!block.isPassable()) {
                    hitBlock = block;
                    break;
                }
            }

            // Move to next point
            currentPoint = currentPoint.add(direction.clone().multiply(stepSize));

            // Check if we've exceeded the maximum distance
            if (currentPoint.distance(eyeLocation.toVector()) > distance) {
                break;
            }
        }

        return new RaycastResult(hitEntities, hitBlock, points);
    }
}