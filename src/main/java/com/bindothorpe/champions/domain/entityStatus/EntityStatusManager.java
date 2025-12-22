package com.bindothorpe.champions.domain.entityStatus;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Function;

/**
 * Manages entity status effects and modifications for entities in the game.
 * This class follows the Singleton pattern and handles the application, removal,
 * and calculation of status effects on entities such as damage modifiers, knockback
 * modifiers, and movement speed changes.
 *
 * <p>Status effects can be either additive (modification values) or multiplicative,
 * and can have optional durations after which they are automatically removed.</p>
 *
 * @author bindothorpe
 */
public class EntityStatusManager {

    private static EntityStatusManager instance;
    private final DomainController dc;

    private static final Map<UUID, Map<EntityStatusType, Set<EntityStatus>>> entityStatuses = new HashMap<>();
    private static final Map<EntityStatus, BukkitTask> tasksMap = new HashMap<>();
    private static final Map<EntityStatusType, Function<UUID, Void>> entityStatusFunctionsMap = new HashMap<>();

    /**
     * Private constructor to enforce Singleton pattern.
     *
     * @param dc The DomainController instance for plugin access
     */
    private EntityStatusManager(DomainController dc) {
        this.dc = dc;
    }

    /**
     * Gets the singleton instance of EntityStatusManager.
     * Creates a new instance if one doesn't already exist.
     *
     * @param dc The DomainController instance
     * @return The singleton EntityStatusManager instance
     */
    public static EntityStatusManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new EntityStatusManager(dc);
        }
        return instance;
    }

    /**
     * Adds an entity status effect to the specified entity.
     *
     * <p>Some status types (DAMAGE_DONE, DAMAGE_RECEIVED, KNOCKBACK_DONE, KNOCKBACK_RECEIVED)
     * are automatically split into both ATTACK and SKILL variants when added.</p>
     *
     * <p>If a status with the same type and source already exists, it will be removed
     * before the new status is added. If the status has a duration, a scheduled task
     * will automatically remove it after the specified time.</p>
     *
     * @param uuid The UUID of the entity to apply the status to
     * @param entityStatus The EntityStatus object containing the status effect details
     */
    public void addEntityStatus(UUID uuid, EntityStatus entityStatus) {

        if(entityStatus.getType().equals(EntityStatusType.DAMAGE_DONE)) {
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.ATTACK_DAMAGE_DONE, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.SKILL_DAMAGE_DONE, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            return;
        } else if(entityStatus.getType().equals(EntityStatusType.DAMAGE_RECEIVED)) {
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.ATTACK_DAMAGE_RECEIVED, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.SKILL_DAMAGE_RECEIVED, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            return;
        } else if (entityStatus.getType().equals(EntityStatusType.KNOCKBACK_DONE)) {
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.ATTACK_KNOCKBACK_DONE, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.SKILL_KNOCKBACK_DONE, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            return;
        } else if (entityStatus.getType().equals(EntityStatusType.KNOCKBACK_RECEIVED)) {
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.ATTACK_KNOCKBACK_RECEIVED, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            addEntityStatus(uuid, new EntityStatus(EntityStatusType.SKILL_KNOCKBACK_RECEIVED, entityStatus.getValue(), entityStatus.getDuration(), entityStatus.isMultiplier(), entityStatus.isAbsolute(), entityStatus.getSource()));
            return;
        }

        //Get the map of statuses for the entity
        Map<EntityStatusType, Set<EntityStatus>> statuses = entityStatuses.get(uuid);

        //If there are no statuses for the entity, create a new map
        if (statuses == null) {
            statuses = new HashMap<>();
            entityStatuses.put(uuid, statuses);
        }

        //Get the set of statuses for the type of status being added
        Set<EntityStatus> statusSet = statuses.get(entityStatus.getType());

        //If there are no statuses for the type of status being added, create a new set
        if (statusSet == null) {
            statusSet = new HashSet<>();
            statuses.put(entityStatus.getType(), statusSet);
        }

        //Remove any statuses of the same type and source
        removeEntityStatus(uuid, entityStatus.getType(), entityStatus.getSource());

        //Add the status to the set
        statusSet.add(entityStatus);


        //If the status has a duration, create a task to remove it after the duration
        if (entityStatus.getDuration() != -1) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    removeEntityStatus(uuid, entityStatus.getType(), entityStatus.getSource());
                    tasksMap.remove(entityStatus);
                    updateEntityStatus(uuid, entityStatus.getType());
                }
            }.runTaskLater(dc.getPlugin(), (long) (entityStatus.getDuration() * 20));
            tasksMap.put(entityStatus, task);
        }
    }

    /**
     * Removes an entity status effect from the specified entity.
     *
     * <p>Some status types (DAMAGE_DONE, DAMAGE_RECEIVED, KNOCKBACK_DONE, KNOCKBACK_RECEIVED)
     * will automatically remove both ATTACK and SKILL variants when removed.</p>
     *
     * <p>If the status had a scheduled removal task, that task will be cancelled.</p>
     *
     * @param uuid The UUID of the entity to remove the status from
     * @param type The type of status effect to remove
     * @param source The source object that applied this status (used to identify which specific status to remove)
     */
    public void removeEntityStatus(UUID uuid, EntityStatusType type, Object source) {
        if(type.equals(EntityStatusType.DAMAGE_DONE)) {
            removeEntityStatus(uuid,EntityStatusType.ATTACK_DAMAGE_DONE, source);
            removeEntityStatus(uuid,EntityStatusType.SKILL_DAMAGE_DONE, source);
            return;
        } else if(type.equals(EntityStatusType.DAMAGE_RECEIVED)) {
            removeEntityStatus(uuid,EntityStatusType.ATTACK_DAMAGE_RECEIVED, source);
            removeEntityStatus(uuid,EntityStatusType.SKILL_DAMAGE_RECEIVED, source);
            return;
        } else if (type.equals(EntityStatusType.KNOCKBACK_DONE)) {
            removeEntityStatus(uuid,EntityStatusType.ATTACK_KNOCKBACK_DONE, source);
            removeEntityStatus(uuid,EntityStatusType.SKILL_KNOCKBACK_DONE, source);
            return;
        } else if (type.equals(EntityStatusType.KNOCKBACK_RECEIVED)) {
            removeEntityStatus(uuid,EntityStatusType.ATTACK_KNOCKBACK_RECEIVED, source);
            removeEntityStatus(uuid,EntityStatusType.SKILL_KNOCKBACK_RECEIVED, source);
            return;
        }
        //Get the map of statuses for the entity
        Map<EntityStatusType, Set<EntityStatus>> statuses = entityStatuses.get(uuid);

        //If there are no statuses for the entity, return
        if (statuses == null) {
            return;
        }

        //Get the set of statuses for the type of status being removed
        Set<EntityStatus> statusSet = statuses.get(type);

        //If there are no statuses for the type of status being removed, return
        if (statusSet == null) {
            return;
        }

        //Remove the status from the set
        statusSet.stream().filter(status -> status.getSource().equals(source)).findFirst().ifPresent(status -> {
            statusSet.remove(status);
            //If the task map contains the status, cancel the task and remove it from the map
            if (tasksMap.containsKey(status)) {
                tasksMap.get(status).cancel();
                tasksMap.remove(status);
            }
        });
    }

    /**
     * Calculates the total additive modification value for a given entity and status type.
     *
     * <p>This method sums all non-multiplier status values for the specified type.
     * If an absolute status exists, only that value is returned.</p>
     *
     * @param uuid The UUID of the entity
     * @param type The type of status effect to calculate
     * @return The total additive modification value, or 0 if no statuses exist
     */
    public double getModifcationValue(UUID uuid, EntityStatusType type) {
        //Get the map of statuses for the entity
        Map<EntityStatusType, Set<EntityStatus>> statuses = entityStatuses.get(uuid);

        //If there are no statuses for the entity, return 0
        if (statuses == null) {
            return EntityStatus.BASE_MOD;
        }

        //Get the set of statuses for the type of status being removed
        Set<EntityStatus> statusSet = statuses.get(type);
        if (statusSet == null) {
            return EntityStatus.BASE_MOD;
        }

        Optional<EntityStatus> optionalStatus = statusSet.stream().filter(status -> !status.isMultiplier()).filter(EntityStatus::isAbsolute).findFirst();
        return optionalStatus.map(EntityStatus::getValue).orElseGet(() -> statusSet.stream().filter(status -> !status.isMultiplier()).map(EntityStatus::getValue).reduce(EntityStatus.BASE_MOD, Double::sum));
    }

    /**
     * Calculates the total multiplicative value for a given entity and status type.
     *
     * <p>This method sums all multiplier status values for the specified type, starting from 1.0.
     * If an absolute multiplier status exists, only that value is returned.</p>
     *
     * @param uuid The UUID of the entity
     * @param type The type of status effect to calculate
     * @return The total multiplicative value, or 1.0 if no statuses exist
     */
    public double getMultiplicationValue(UUID uuid, EntityStatusType type) {
        //Get the map of statuses for the entity
        Map<EntityStatusType, Set<EntityStatus>> statuses = entityStatuses.get(uuid);

        //If there are no statuses for the entity, return 1
        if (statuses == null) {
            return EntityStatus.BASE_MULT;
        }

        //Get the set of statuses for the type of status being removed
        Set<EntityStatus> statusSet = statuses.get(type);
        if (statusSet == null) {
            return EntityStatus.BASE_MULT;
        }

        Optional<EntityStatus> optionalStatus = statusSet.stream().filter(EntityStatus::isMultiplier).filter(EntityStatus::isAbsolute).findFirst();
        return optionalStatus.map(EntityStatus::getValue).orElseGet(() -> statusSet.stream().filter(EntityStatus::isMultiplier).map(EntityStatus::getValue).reduce(EntityStatus.BASE_MULT, Double::sum));
    }

    /**
     * Calculates the final value after applying all status modifications.
     *
     * <p>The calculation follows the formula: (baseValue + modificationValue) * multiplicationValue</p>
     *
     * @param uuid The UUID of the entity
     * @param type The type of status effect to calculate
     * @param baseValue The base value before modifications
     * @return The final calculated value after applying all status effects
     */
    public double getFinalValue(UUID uuid, EntityStatusType type, double baseValue) {
        return (baseValue + getModifcationValue(uuid, type)) * getMultiplicationValue(uuid, type);
    }

    /**
     * Updates the entity's actual state based on the current status effects.
     *
     * <p>This method applies the calculated status values to the entity's actual properties
     * (e.g., updating a player's walk speed for MOVEMENT_SPEED status).</p>
     *
     * <p>The function map is lazily initialized on first call.</p>
     *
     * @param uuid The UUID of the entity to update
     * @param type The type of status effect that needs to be applied
     */
    public void updateEntityStatus(UUID uuid, EntityStatusType type) {
        if (entityStatusFunctionsMap.isEmpty()) {
            populate();
        }

        if (!entityStatusFunctionsMap.containsKey(type))
            return;

        entityStatusFunctionsMap.get(type).apply(uuid);
    }

    /**
     * Populates the entity status functions map with handlers for different status types.
     *
     * <p>Currently implements:
     * <ul>
     *   <li>MOVEMENT_SPEED: Updates player walk speed, clamped between -1.0 and 1.0</li>
     * </ul>
     * </p>
     *
     * <p>This method is called lazily when updateEntityStatus is first invoked.</p>
     */
    private void populate() {
        entityStatusFunctionsMap.put(EntityStatusType.MOVEMENT_SPEED, (uuid) -> {
            //Get the entity
            Entity e = Bukkit.getEntity(uuid);

            //If the entity is null, return null
            if (e == null)
                return null;

            if(!(e instanceof Player player)) return null;
            float defaultMovementSpeed = 0.2f;

            //Set the walking speed of the player
            if (getMultiplicationValue(uuid, EntityStatusType.MOVEMENT_SPEED) == 0)
                player.setWalkSpeed(0);
            else
                player.setWalkSpeed((float) Math.clamp(getFinalValue(uuid, EntityStatusType.MOVEMENT_SPEED, defaultMovementSpeed), -1f, 1f));
            return null;
        });
    }

}