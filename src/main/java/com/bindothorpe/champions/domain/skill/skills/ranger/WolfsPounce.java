package com.bindothorpe.champions.domain.skill.skills.ranger;

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
import com.bindothorpe.champions.util.EntityUtil;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class WolfsPounce extends ChargeSkill {
    private final List<Double> collisionDamage = List.of(3d, 4d, 5d);
    private final Map<UUID, Double> active = new HashMap();

    public WolfsPounce(DomainController dc) {
        super(dc, SkillId.WOLFS_POUNCE, SkillType.SWORD, ClassType.RANGER, "Wolfs Pounce", List.of(12d, 10d, 7d), 3, 1, List.of(40, 30, 15), List.of(5d, 5d, 5d));
    }

    private void handleWolfsPounceCollide(Player player, Entity entity, double chargePercentage) {
        dc.getSoundManager().playSound(entity.getLocation(), CustomSound.SKILL_WOLFS_POUNCE_COLLIDE);

        MobilityUtil.stopVelocity(player);
        active.remove(player.getUniqueId());

        CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) entity, player, collisionDamage.get(getSkillLevel(player.getUniqueId()) - 1) * chargePercentage, player.getLocation(), CustomDamageSource.SKILL);
        CustomDamageCommand damageCommand = new CustomDamageCommand(dc, damageEvent);
        damageEvent.setCommand(damageCommand);

        Bukkit.getPluginManager().callEvent(damageEvent);

        if (damageEvent.isCancelled()) {
            return;
        }

        damageCommand.execute();
    }

    private void handleWolfsPounce(UUID uuid, int charge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        activate(uuid, null);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_WOLFS_POUNCE);
        MobilityUtil.launch(player, 0.4 + 0.05 * charge, 0.2, 0.6 + 0.01 * charge, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                double chargePercentage = (double) Math.min(charge, getMaxCharge(uuid)) / getMaxCharge(uuid);
                active.put(uuid, chargePercentage);
            }
        }.runTaskLater(dc.getPlugin(), 2L);
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
    protected void onUpdate(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        // Only proceed if the WolfsPounce skill is active for this player
        if (!active.containsKey(uuid))
            return;


        List<Entity> nearby = EntityUtil.getCollidingEntities(player).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .collect(Collectors.toList());

        nearby.remove(player);
        double chargePercentage = active.get(uuid);

        if (nearby.isEmpty()) {
            if (((Entity) player).isOnGround()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        active.remove(uuid);
                    }
                }.runTaskLater(dc.getPlugin(), 5L);
            }
            return;
        }

        handleWolfsPounceCollide(player, nearby.get(0), chargePercentage);
    }


    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        return lore;
    }


}
