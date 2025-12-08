package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.FlameItem;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class Inferno extends ChargeSkill {

    private static final Random random = new Random();

    public Inferno(DomainController dc) {
        super(dc, SkillId.INFERNO, SkillType.SWORD, ClassType.MAGE, "Inferno", List.of(10D, 8D, 6D), 3, 1, List.of(1, 1, 1), List.of(3D, 3.5D, 4D));
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        stopAndStartCooldown(uuid);
    }

    @Override
    protected void onMaxChargeReached(UUID uuid, int charge) {}

    @Override
    protected void onCharge(UUID uuid, int charge) {
        Player player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
        spawnFlame(player);
    }

    @Override
    protected void onChargeStart(UUID uuid) {}

    @Override
    protected void onChargeEnd(UUID uuid, int charge) {
        stopAndStartCooldown(uuid);
    }

    private void stopAndStartCooldown(UUID uuid) {
        startCooldown(uuid);
    }

    private void spawnFlame(Player player) {
        GameItem flameItem = new FlameItem(dc,
                player,
                1,
                getId());
        Vector direction = player.getLocation().getDirection().clone().add(new Vector(
                0.07D - random.nextInt(14) / 100.0D,
                0.07D - random.nextInt(14) / 100.0D,
                0.07D - random.nextInt(14) / 100.0D
        ));
        dc.getGameItemManager().spawnGameItem(flameItem, player.getEyeLocation().clone().add(0, -0.3, 0), direction, 1.6);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_INFERNO_FLAME_SPAWN);
    }

    @Override
    protected void onUpdate(UUID uuid) {}
}
