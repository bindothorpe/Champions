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
    private final List<Integer> maxCharge;
    private final List<Double> maxChargeDuration;
    private final Map<UUID, Integer> chargeMap = new HashMap<>();
    private final Map<UUID, Long> chargeStartMap = new HashMap<>();
    private final Set<UUID> maxCharged = new HashSet<>();

    public ChargeSkill(DomainController dc, SkillId id, SkillType skillType, ClassType classType, String name, List<Double> cooldownDuration, int maxLevel, int levelUpCost, List<Integer> maxCharge, List<Double> maxChargeDuration) {
        super(dc, id, skillType, classType, name, cooldownDuration, maxLevel, levelUpCost);
        this.maxCharge = maxCharge;
        this.maxChargeDuration = maxChargeDuration;
    }

    protected int getMaxCharge(UUID uuid) {
        if(!isUser(uuid))
            return -1;
        return maxCharge.get(getSkillLevel(uuid) - 1);
    }

    protected double getChargePercentage(UUID uuid) {
        if(!isUser(uuid))
            return -1;
        return Math.min((double) chargeMap.get(uuid) / maxCharge.get(getSkillLevel(uuid) - 1), 1);
    }


    protected abstract void onMaxChargeDurationReached(UUID uuid, int charge);

    protected abstract void onMaxChargeReached(UUID uuid, int charge);

    protected abstract void onCharge(UUID uuid, int charge);

    protected abstract void onChargeStart(UUID uuid);

    protected abstract void onChargeEnd(UUID uuid, int charge);
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

                int maxChargeValue = maxCharge.get(getSkillLevel(uuid) - 1);

                //Check if the charge is at max
                if (chargeMap.get(uuid) >= maxChargeValue && !maxCharged.contains(uuid)) {
                    onMaxChargeReached(uuid, maxChargeValue);
                    maxCharged.add(uuid);
                }

                //Check if the charge has reached the max duration
                if (System.currentTimeMillis() - chargeStartMap.get(uuid) >= maxChargeDuration.get(getSkillLevel(uuid) - 1) * 1000) {

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
