package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WolfsPounce extends ChargeSkill {
    public WolfsPounce(DomainController dc) {
        super(dc, SkillId.WOLFS_POUNCE, SkillType.SWORD, ClassType.RANGER, "Wolfs Pounce", List.of(12d, 10d, 7d), 3, 1, List.of(40, 30, 15), List.of(5d, 5d, 5d));
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        handleWolfsPounce(uuid, charge);
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
        handleWolfsPounce(uuid, charge);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        return lore;
    }

    private void handleWolfsPounce(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        activate(uuid, null);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_WOLFS_POUNCE);
        MobilityUtil.launch(player, 0.4 + 0.05 * charge, 0.2, 0.6 + 0.01 * charge, true);
    }


}
