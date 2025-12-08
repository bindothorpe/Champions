package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
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

public class StaticLazer extends ChargeSkill {

    private static final double BASE_DISTANCE = 30D;
    private static final double ADDITIONAL_DISTANCE_PER_LEVEL = 10D;

    private static final double BASE_DAMAGE = 6D;
    private static final double ADDITIONAL_DAMAGE_PER_LEVEL = 2D;

    public StaticLazer(DomainController dc) {
        super(dc, SkillId.STATIC_LAZER, SkillType.SWORD, ClassType.MAGE, "Static Lazer", List.of(9.5D, 9D, 8.5D, 8D, 7.5D), 5, 1, List.of(40, 35, 30, 25, 20), List.of(3D, 3D, 3D, 3D, 3D));
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

        RaycastResult result = RaycastUtil.drawRaycastFromPlayerInLookingDirection(
                player,
                BASE_DISTANCE + (getSkillLevel(uuid) * ADDITIONAL_DISTANCE_PER_LEVEL) * ((double) charge / getMaxCharge(uuid)),
                2,
                0.3,
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

        CustomDamageEvent customDamageEvent = new CustomDamageEvent(
                dc,
                hitEntity,
                player,
                null,
                BASE_DAMAGE + (getSkillLevel(uuid) * ADDITIONAL_DAMAGE_PER_LEVEL) * ((double) charge / getMaxCharge(uuid)),
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
}
