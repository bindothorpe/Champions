package com.bindothorpe.champions.domain.entityStatus;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Function;

public class EntityStatusManager {

    private static EntityStatusManager instance;
    private final DomainController dc;

    private static final Map<UUID, Map<EntityStatusType, Set<EntityStatus>>> entityStatuses = new HashMap<>();
    private static final Map<EntityStatus, BukkitTask> tasksMap = new HashMap<>();
    private static final Map<EntityStatusType, Function<UUID, Void>> entityStatusFunctionsMap = new HashMap<>();

    private EntityStatusManager(DomainController dc) {
        this.dc = dc;
    }

    public static EntityStatusManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new EntityStatusManager(dc);
        }
        return instance;
    }

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

    public double getModifcationValue(UUID uuid, EntityStatusType type) {
        //Get the map of statuses for the entity
        Map<EntityStatusType, Set<EntityStatus>> statuses = entityStatuses.get(uuid);

        //If there are no statuses for the entity, return 0
        if (statuses == null) {
            return 0;
        }

        //Get the set of statuses for the type of status being removed
        Set<EntityStatus> statusSet = statuses.get(type);
        if (statusSet == null) {
            return 0;
        }

        Optional<EntityStatus> optionalStatus = statusSet.stream().filter(status -> !status.isMultiplier()).filter(EntityStatus::isAbsolute).findFirst();
        if (optionalStatus.isPresent()) {
            return optionalStatus.get().getValue();
        }

        //Return the sum of the values of all the statuses that are not multipliers
        return statusSet.stream().filter(status -> !status.isMultiplier()).map(EntityStatus::getValue).reduce(0.0, Double::sum);
    }

    public double getMultiplicationValue(UUID uuid, EntityStatusType type) {
        //Get the map of statuses for the entity
        Map<EntityStatusType, Set<EntityStatus>> statuses = entityStatuses.get(uuid);

        //If there are no statuses for the entity, return 1
        if (statuses == null) {
            return 1;
        }

        //Get the set of statuses for the type of status being removed
        Set<EntityStatus> statusSet = statuses.get(type);
        if (statusSet == null) {
            return 1;
        }

        Optional<EntityStatus> optionalStatus = statusSet.stream().filter(EntityStatus::isMultiplier).filter(EntityStatus::isAbsolute).findFirst();
        if (optionalStatus.isPresent()) {
            return optionalStatus.get().getValue();
        }

        //Return the sum of the values of all the statuses that are multipliers
        return statusSet.stream().filter(EntityStatus::isMultiplier).map(EntityStatus::getValue).reduce(1.0, (a, b) -> a + b);
    }

    public double getFinalValue(UUID uuid, EntityStatusType type, double baseValue) {
        return (baseValue + getModifcationValue(uuid, type)) * getMultiplicationValue(uuid, type);
    }


    public void updateEntityStatus(UUID uuid, EntityStatusType type) {
        if (entityStatusFunctionsMap.isEmpty()) {
            populate();
        }

        if (!entityStatusFunctionsMap.containsKey(type))
            return;

        entityStatusFunctionsMap.get(type).apply(uuid);
    }

    private void populate() {
        entityStatusFunctionsMap.put(EntityStatusType.MOVEMENT_SPEED, (uuid) -> {
            //Get the entity
            Entity e = Bukkit.getEntity(uuid);

            //If the entity is null, return null
            if (e == null)
                return null;

            //If the entity is not a player, return null
            if (!(e instanceof Player))
                return null;

            Player player = (Player) e;
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
