package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

public abstract class ChargeSkill extends Skill {
    private final Map<UUID, Integer> chargeMap = new HashMap<>();
    private final Map<UUID, Long> chargeStartMap = new HashMap<>();
    private final Set<UUID> maxCharged = new HashSet<>();

    protected static int BASE_MAX_CHARGE;
    protected static int MAX_CHARGE_REDUCTION_PER_LEVEL;
    protected static double BASE_MAX_CHARGE_DURATION;
    protected static double MAX_CHARGE_DURATION_INCREASE_PER_LEVEL;


    public ChargeSkill(DomainController dc, String name, SkillId id, SkillType skillType, ClassType classType) {
        super(dc, name, id, skillType, classType);
    }

    protected int getMaxCharge(UUID uuid) {
        if(!isUser(uuid))
            return -1;
        return calculateBasedOnLevel(BASE_MAX_CHARGE, -MAX_CHARGE_REDUCTION_PER_LEVEL, getSkillLevel(uuid));
    }

    protected double getChargePercentage(UUID uuid) {
        if(!isUser(uuid))
            return -1;
        return Math.min((double) chargeMap.get(uuid) / getMaxCharge(uuid), 1);
    }

    /**
     * Called when the maximum charge duration has been reached for a player.
     * This occurs when the player has been charging for the full duration allowed
     * by their skill level, regardless of whether they've reached max charge.
     *
     * @param uuid the UUID of the player who reached max charge duration
     * @param charge the current charge level when the duration was reached
     */
    protected abstract void onMaxChargeDurationReached(UUID uuid, int charge);

    /**
     * Called when a player reaches the maximum charge level for their skill.
     * This is triggered the first time the charge reaches the max value.
     *
     * @param uuid the UUID of the player who reached max charge
     * @param charge the maximum charge value that was reached
     */
    protected abstract void onMaxChargeReached(UUID uuid, int charge);

    /**
     * Called every tick while a player is charging their skill.
     * This allows for continuous updates during the charging process.
     *
     * @param uuid the UUID of the player who is charging
     * @param charge the current charge level
     */
    protected abstract void onCharge(UUID uuid, int charge);

    /**
     * Called when a player begins charging their skill.
     * This is triggered on the first tick of blocking when the skill can be used.
     *
     * @param uuid the UUID of the player who started charging
     */
    protected abstract void onChargeStart(UUID uuid);

    /**
     * Called when a player stops charging their skill before reaching max duration.
     * This occurs when the player stops blocking.
     *
     * @param uuid the UUID of the player who stopped charging
     * @param charge the charge level when charging was stopped
     */
    protected abstract void onChargeEnd(UUID uuid, int charge);

    /**
     * Called every tick for each user of this skill, regardless of charging state.
     * This allows for custom update logic to be implemented per skill.
     *
     * @param uuid the UUID of the player being updated
     */
    protected abstract void onUpdate(UUID uuid);

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for (UUID uuid : getUsers()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null)
                continue;

            onUpdate(uuid);

            //Check if the player is blocking
            if (player.isBlocking()) {

                //Check if the player can use the skill
                if (!canUse(uuid, null))
                    return;

                //Check if the chargeMap contains the player
                if (!chargeMap.containsKey(uuid)) {
                    chargeMap.put(uuid, 1);
                    chargeStartMap.put(uuid, System.currentTimeMillis());
                    onChargeStart(uuid);
                    onCharge(uuid, 1);
                } else {
                    int charge = chargeMap.get(uuid) + 1;
                    chargeMap.put(uuid, charge);
                    onCharge(uuid, charge);
                }

                int maxChargeValue = getMaxCharge(uuid);

                //Check if the charge is at max
                if (chargeMap.get(uuid) >= maxChargeValue && !maxCharged.contains(uuid)) {
                    onMaxChargeReached(uuid, maxChargeValue);
                    maxCharged.add(uuid);
                }

                //Check if the charge has reached the max duration
                if (System.currentTimeMillis() - chargeStartMap.get(uuid) >= calculateBasedOnLevel(BASE_MAX_CHARGE_DURATION, MAX_CHARGE_DURATION_INCREASE_PER_LEVEL, getSkillLevel(uuid)) * 1000) {

                    int charge = chargeMap.get(uuid);
                    onMaxChargeDurationReached(uuid, charge);
                    chargeMap.remove(uuid);
                }

            } else {

                if (!chargeMap.containsKey(uuid))
                    continue;

                int charge = chargeMap.get(uuid);
                chargeMap.remove(uuid);
                onChargeEnd(uuid, charge);
            }
        }
    }
}