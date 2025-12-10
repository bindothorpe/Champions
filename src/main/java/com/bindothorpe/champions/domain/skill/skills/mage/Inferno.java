package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.FlameItem;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class Inferno extends ChargeSkill implements ReloadableData {

    private static final Random random = new Random();
    private static double BASE_FLAME_DAMAGE;
    private static double FLAME_DAMAGE_INCREASE_PER_LEVEL;
    private static double BASE_LAUNCH_STRENGTH;
    private static double LAUNCH_STRENGTH_INCREASE_PER_LEVEL;

    public Inferno(DomainController dc) {
        super(dc, "Inferno", SkillId.INFERNO, SkillType.SWORD, ClassType.MAGE);
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
        double flameDamage = calculateBasedOnLevel(BASE_FLAME_DAMAGE, FLAME_DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));
        double launchStrength = calculateBasedOnLevel(BASE_LAUNCH_STRENGTH, LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));

        GameItem flameItem = new FlameItem(dc,
                player,
                flameDamage,
                getId());
        Vector direction = player.getLocation().getDirection().clone().add(new Vector(
                0.07D - random.nextInt(14) / 100.0D,
                0.07D - random.nextInt(14) / 100.0D,
                0.07D - random.nextInt(14) / 100.0D
        ));
        dc.getGameItemManager().spawnGameItem(flameItem, player.getEyeLocation().clone().add(0, -0.3, 0), direction, launchStrength);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_INFERNO_FLAME_SPAWN);
    }

    @Override
    protected void onUpdate(UUID uuid) {}

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.inferno.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.inferno.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.cooldown_reduction_per_level");
            BASE_MAX_CHARGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.inferno.base_max_charge");
            MAX_CHARGE_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.inferno.max_charge_reduction_per_level");
            BASE_MAX_CHARGE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.base_max_charge_duration");
            MAX_CHARGE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.max_charge_duration_increase_per_level");
            BASE_FLAME_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.base_flame_damage");
            FLAME_DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.flame_damage_increase_per_level");
            BASE_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.base_launch_strength");
            LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.inferno.launch_strength_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}