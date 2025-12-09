package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BullsCharge extends Skill implements ReloadableData {

    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;
    private static double BASE_ACTIVE_DURATION;
    private static double ACTIVE_DURATION_INCREASE_PER_LEVEL;
    private static int MOVE_SPEED_EFFECT;
    private static int SLOW_EFFECT;
    private static double BASE_SLOW_EFFECT_DURATION;
    private static double SLOW_EFFECT_DURATION_INCREASE_PER_LEVEL;

    private final Map<UUID, Integer> activeMap = new HashMap<>();

    public BullsCharge(DomainController dc) {
        super(dc, "Bull's Charge", SkillId.BULLS_CHARGE, SkillType.AXE, ClassType.KNIGHT);
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerRightClickEvent playerRightClickEvent))
            return false;

        if (!playerRightClickEvent.isAxe())
            return false;

        return super.canUseHook(uuid, event);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if (!activate(event.getPlayer().getUniqueId(), event)) {
            return;
        }

        Player player = event.getPlayer();
        double duration = calculateBasedOnLevel(BASE_ACTIVE_DURATION, ACTIVE_DURATION_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));

        StatusEffectManager.getInstance(dc).addStatusEffectToEntity(StatusEffectType.SPEED, player.getUniqueId(), getNamespacedKey(player), MOVE_SPEED_EFFECT, duration);
        int taskId = dc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(
                dc.getPlugin(),
                () -> {
                    activeMap.remove(player.getUniqueId());
                },
                (long) (duration * 20L)
        );

        activeMap.put(player.getUniqueId(), taskId);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_BULLS_CHARGE_ACTIVATE);
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        Entity damager = event.getDamager();
        if(!activeMap.containsKey(damager.getUniqueId())) return;

        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;

        Entity damagee = event.getDamagee();

        dc.getPlugin().getServer().getScheduler().cancelTask(activeMap.get(damager.getUniqueId()));

        StatusEffectManager.getInstance(dc).removeStatusEffectFromPlayer(StatusEffectType.SPEED, damager.getUniqueId(), getNamespacedKey(damager.getUniqueId()));

        double slowDuration = calculateBasedOnLevel(BASE_SLOW_EFFECT_DURATION, SLOW_EFFECT_DURATION_INCREASE_PER_LEVEL, getSkillLevel(damager.getUniqueId()));
        StatusEffectManager.getInstance(dc).addStatusEffectToEntity(StatusEffectType.SLOW, damagee.getUniqueId(), getNamespacedKey(damager.getUniqueId()), SLOW_EFFECT, slowDuration);

        double bonusDamage = calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(damager.getUniqueId()));
        event.getCommand().damage(event.getCommand().getDamage() + bonusDamage);

        dc.getSoundManager().playSound(event.getDamagee().getLocation(), CustomSound.SKILL_BULLS_CHARGE_ACTIVATE);

        activeMap.remove(damager.getUniqueId());
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @Override
    public void onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.knight.bulls_charge.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.knight.bulls_charge.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.cooldown_reduction_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.damage_increase_per_level");
            BASE_ACTIVE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.base_active_duration");
            ACTIVE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.active_duration_increase_per_level");
            MOVE_SPEED_EFFECT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.knight.bulls_charge.move_speed_effect");
            SLOW_EFFECT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.knight.bulls_charge.slow_effect");
            BASE_SLOW_EFFECT_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.base_slow_effect_duration");
            SLOW_EFFECT_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.bulls_charge.slow_effect_duration_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}