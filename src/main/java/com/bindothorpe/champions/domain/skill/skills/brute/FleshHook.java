package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.ExplosiveItem;
import com.bindothorpe.champions.domain.item.items.FleshHookItem;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class FleshHook extends ChargeSkill {

    private final Map<UUID, GameItem> fleshHookItemMap = new HashMap<>();
    private final List<Double> fleshHookDamageMap = Arrays.asList(2d, 3d, 4d, 5d, 6d);


    public FleshHook(DomainController dc) {
        super(dc, SkillId.FLESH_HOOK, SkillType.SWORD, ClassType.BRUTE, "Flesh Hook", List.of(14d, 13d, 12d, 11d, 10d), 5, 1, List.of(45, 40, 35, 30, 25), List.of(5d, 5d, 5d, 5d, 5d));
    }

    private void handleFleshHookShoot(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        if (!activate(player.getUniqueId(), null))
            return;

        dc.getSoundManager().playSound(player, CustomSound.SKILL_FLESH_HOOK_THROW);

        GameItem fleshHookItem = new FleshHookItem(dc,
                player,
                fleshHookDamageMap.get(getSkillLevel(player.getUniqueId()) - 1),
                1.5D + 0.3D * getSkillLevel(uuid));
        dc.getGameItemManager().spawnGameItem(fleshHookItem, player.getEyeLocation().clone().add(0, -0.3, 0), player.getLocation().getDirection(), 1.5);
        fleshHookItemMap.put(player.getUniqueId(), fleshHookItem);
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        handleFleshHookShoot(uuid, charge);
    }

    @Override
    protected void onMaxChargeReached(UUID uuid, int charge) {

    }

    @Override
    protected void onCharge(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        dc.getSoundManager().playSound(player, CustomSound.CHARGE_SKILL_CHARGE, getChargePercentage(uuid));
        ChatUtil.sendActionBarMessage(player, ComponentUtil.chargeBar(charge, getMaxCharge(uuid)));
    }

    @Override
    protected void onChargeStart(UUID uuid) {

    }

    @Override
    protected void onChargeEnd(UUID uuid, int charge) {
        handleFleshHookShoot(uuid, charge);
    }

    @Override
    protected void onUpdate(UUID uuid) {

    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
