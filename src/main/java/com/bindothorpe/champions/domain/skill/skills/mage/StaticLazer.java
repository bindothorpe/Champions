package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.raycast.RaycastResult;
import com.bindothorpe.champions.util.raycast.RaycastUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class StaticLazer extends ChargeSkill implements ReloadableData {

    private static double BASE_DISTANCE;
    private static double DISTANCE_INCREASE_PER_LEVEL;
    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;
    private static double DETECTION_RADIUS;
    private static double DETECTION_DENSITY_PER_BLOCK;

    public StaticLazer(DomainController dc) {
        super(dc, "Static Lazer", SkillId.STATIC_LAZER, SkillType.SWORD, ClassType.MAGE);
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        handleStaticLaser(uuid, charge);
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
        handleStaticLaser(uuid, charge);
    }

    @Override
    protected void onUpdate(UUID uuid) {
    }

    private void handleStaticLaser(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        double distance = calculateBasedOnLevel(BASE_DISTANCE, DISTANCE_INCREASE_PER_LEVEL, getSkillLevel(uuid));
        double chargePercentage = (double) charge / getMaxCharge(uuid);

        RaycastResult result = RaycastUtil.drawRaycastFromPlayerInLookingDirection(
                player,
                distance * chargePercentage,
                DETECTION_DENSITY_PER_BLOCK,
                DETECTION_RADIUS,
                false,
                false,
                false
        );

        World world = player.getWorld();

        for(Vector vector : result.raycastPoints()) {
            // Spawn Firework particle on all raycastPoints
            Location location = vector.toLocation(world);
            world.spawnParticle(Particle.FIREWORK, location, 1, 0, 0, 0, 0);
        }

        activate(uuid, null);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_STATIC_LAZER_SHOOT);

        if(result.getFirstHit() == null) return;


        LivingEntity hitEntity = result.getFirstHit();

        Vector lastPoint = result.raycastPoints().getLast();
        Location explosionLocation = lastPoint.toLocation(world);
        world.spawnParticle(Particle.FIREWORK, explosionLocation, 50, 0.3, 0.3, 0.3, 0.1);

        dc.getSoundManager().playSound(explosionLocation, CustomSound.SKILL_STATIC_LAZER_HIT);

        double damage = calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(uuid));

        CustomDamageEvent customDamageEvent = new CustomDamageEvent(
                dc,
                hitEntity,
                player,
                null,
                damage * chargePercentage,
                player.getLocation(),
                CustomDamageSource.SKILL,
                getName()
        );

        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, customDamageEvent);
        customDamageEvent.setCommand(customDamageCommand);


        Bukkit.getPluginManager().callEvent(customDamageEvent);

        if (customDamageEvent.isCancelled()) {
            return;
        }

        customDamageCommand.execute();
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @Override
    public void onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.static_lazer.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.static_lazer.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.cooldown_reduction_per_level");
            BASE_MAX_CHARGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.static_lazer.base_max_charge");
            MAX_CHARGE_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.static_lazer.max_charge_reduction_per_level");
            BASE_MAX_CHARGE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.base_max_charge_duration");
            MAX_CHARGE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.max_charge_duration_increase_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.damage_increase_per_level");
            BASE_DISTANCE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.base_distance");
            DISTANCE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.distance_increase_per_level");
            DETECTION_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.detection_radius");
            DETECTION_DENSITY_PER_BLOCK = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.static_lazer.detection_density_per_block");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}