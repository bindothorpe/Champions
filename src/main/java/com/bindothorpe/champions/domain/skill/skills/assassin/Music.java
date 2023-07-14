package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Music extends ChargeSkill {
    public Music(DomainController dc) {
        super(dc, SkillId.MUSIC, SkillType.SWORD, ClassType.ASSASSIN, "Music", List.of(10d, 8d, 6d), 3, 1, List.of(40, 30, 20), List.of(5d, 5d, 5d));
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_MUSIC_DURATION_MAX);
        startCooldown(uuid);
        ChatUtil.sendMessage(player, ChatUtil.Prefix.DEBUG, Component.text("Max duration reached!").color(NamedTextColor.GOLD));
        ChatUtil.sendActionBarMessage(player, Component.empty());
    }

    @Override
    protected void onMaxChargeReached(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_MUSIC_CHARGE_MAX);
        ChatUtil.sendMessage(player, ChatUtil.Prefix.DEBUG, Component.text("Max charge reached!").color(NamedTextColor.GOLD));
    }

    @Override
    protected void onCharge(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_MUSIC_CHARGE, getChargePercentage(uuid));

        ChatUtil.sendActionBarMessage(player, ComponentUtil.chargeBar(charge, getMaxCharge(uuid)));
    }

    @Override
    protected void onChargeStart(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_MUSIC_CHARGE_START);
    }

    @Override
    protected void onChargeEnd(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_MUSIC_CHARGE_END);
        startCooldown(uuid);
        ChatUtil.sendMessage(player, ChatUtil.Prefix.DEBUG, Component.text("Ended charge!").color(NamedTextColor.GOLD));
        ChatUtil.sendActionBarMessage(player, Component.empty());
    }

    @Override
    protected void onUpdate(UUID uuid) {

    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        return lore;
    }
}
