package com.bindothorpe.champions.util.raycast;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;

/**
 * Represents the result of a raycast operation.
 * Contains information about all entities hit, any block hit, and the path traced by the raycast.
 *
 * @param livingEntitiesHit A set of all living entities that were hit during the raycast.
 *                          May be empty if no entities were hit
 * @param blockHit The first non-passable block hit during the raycast.
 *                 Will be null if no block was hit or if passTroughTerrain was true
 * @param raycastPoints A list of all vector points along the raycast path.
 *                      Useful for visualizing the raycast with particles or debugging
 */
public record RaycastResult(Set<LivingEntity> livingEntitiesHit, Block blockHit, List<Vector> raycastPoints) {

    /**
     * Gets the first living entity that was hit during the raycast.
     * The "first" entity is determined by the iteration order of the internal set,
     * which typically corresponds to the first entity encountered along the raycast path.
     *
     * @return The first living entity hit, or null if no entities were hit
     */
    public LivingEntity getFirstHit() {
        if (livingEntitiesHit == null || livingEntitiesHit.isEmpty()) {
            return null;
        }
        return livingEntitiesHit.iterator().next();
    }
}