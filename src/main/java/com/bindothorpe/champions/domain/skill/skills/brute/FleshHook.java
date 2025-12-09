package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.ExplosiveItem;
import com.bindothorpe.champions.domain.item.items.FleshHookItem;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class FleshHook extends ChargeSkill implements ReloadableData {

    private final Map<UUID, GameItem> fleshHookItemMap = new HashMap<>();

    protected  static double BASE_DAMAGE;
    protected  static double DAMAGE_INCREASE_PER_LEVEL;
    protected  static double BASE_LAUNCH_STRENGTH;
    protected  static double LAUNCH_STRENGTH_INCREASE_PER_LEVEL;
    protected  static double BASE_PULL_STRENGTH;
    protected  static double PULL_STRENGTH_INCREASE_PER_LEVEL;

    public FleshHook(DomainController dc) {
        super(dc, "Flesh Hook", SkillId.FLESH_HOOK, SkillType.SWORD, ClassType.BRUTE);
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
                calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(uuid)),
                calculateBasedOnLevel(BASE_PULL_STRENGTH, PULL_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(uuid)));
        dc.getGameItemManager().spawnGameItem(
                fleshHookItem,
                player.getEyeLocation().clone().add(0, -0.3, 0),
                player.getLocation().getDirection(),
                calculateBasedOnLevel(BASE_LAUNCH_STRENGTH, LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(uuid)));
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
        ChatUtil.sendActionBarMessage(player, ComponentUtil.skillCharge(getName(), true, charge, getMaxCharge(uuid)));
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


    @Override
    public void onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.cooldown_reduction_per_level");
            BASE_MAX_CHARGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.base_max_charge");
            MAX_CHARGE_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.max_charge_reduction_per_level");
            BASE_MAX_CHARGE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.base_max_charge_duration");
            MAX_CHARGE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.max_charge_duration_increase_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.damage_increase_per_level");
            BASE_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.base_launch_strength");
            LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.launch_strength_increase_per_level");
            BASE_PULL_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.base_pull_strength");
            PULL_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.flesh_hook.pull_strength_increase_per_level");

            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}
