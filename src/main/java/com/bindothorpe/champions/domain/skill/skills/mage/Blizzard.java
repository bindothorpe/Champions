package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.subSkills.ChargeSkill;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Blizzard extends ChargeSkill implements ReloadableData {

    private static final Random random = new Random();
    private static double BASE_LAUNCH_STRENGTH;
    private static double LAUNCH_STRENGTH_INCREASE_PER_LEVEL;
    private static double BASE_IMPACT_LAUNCH_STRENGTH_MODIFIER;
    private static double IMPACT_LAUNCH_STRENGTH_MODIFIER_INCREASE_PER_LEVEL;

    public Blizzard(DomainController dc) {
        super(dc, "Blizzard", SkillId.BLIZZARD, SkillType.SWORD, ClassType.MAGE);
    }

    @Override
    protected void onMaxChargeDurationReached(UUID uuid, int charge) {
        startCooldown(uuid);
    }

    @Override
    protected void onMaxChargeReached(UUID uuid, int charge) {}

    @Override
    protected void onCharge(UUID uuid, int charge) {
        Player player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
        spawnSnowball(player);
    }

    @Override
    protected void onChargeStart(UUID uuid) {}

    @Override
    protected void onChargeEnd(UUID uuid, int charge) {
        startCooldown(uuid);
    }

    @Override
    protected void onUpdate(UUID uuid) {}

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    private void spawnSnowball(Player player) {
        for (int i = 0; i < 2; i++) {
            Projectile snowball = player.launchProjectile(Snowball.class);

            CustomDamageEvent.addCustomDamageSourceData(dc, snowball, CustomDamageSource.SKILL_PROJECTILE, true);
            CustomDamageEvent.addSkillIdData(dc, snowball, getId());

            double x = 0.2D - random.nextInt(40) / 100.0D;
            double y = random.nextInt(20) / 100.0D;
            double z = 0.2D - random.nextInt(40) / 100.0D;

            double launchStrength = calculateBasedOnLevel(BASE_LAUNCH_STRENGTH, LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));
            snowball.setVelocity(player.getLocation().getDirection().add(new Vector(x, y, z)).multiply(launchStrength));

            dc.getSoundManager().playSound(player.getEyeLocation(), CustomSound.SKILL_BLIZZARD_SNOWBALL_SPAWN);

        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSnowballHit(CustomDamageEvent event) {
        if(!(event.getProjectile() != null && event.getProjectile() instanceof Snowball snowball)) return;

        if(!CustomDamageEvent.hasSkillIdData(dc, snowball)) return;

        if(CustomDamageEvent.getSkillIdData(dc, snowball) != getId()) return;

        if(!CustomDamageEvent.hasCustomDamageSourceData(dc, snowball)) return;

        if(CustomDamageEvent.getCustomDamageSourceData(dc, snowball) != CustomDamageSource.SKILL_PROJECTILE) return;

        if(event.getDamagee() == null) return;

        if(!(snowball.getShooter() instanceof Player shooter)) return;

        event.getCommand().suppressHitSound();
        event.getCommand().damage(0);

        double impactModifier = calculateBasedOnLevel(BASE_IMPACT_LAUNCH_STRENGTH_MODIFIER, IMPACT_LAUNCH_STRENGTH_MODIFIER_INCREASE_PER_LEVEL, getSkillLevel(shooter.getUniqueId()));
        event.getCommand().force(snowball.getVelocity().multiply(impactModifier).length());
        event.getCommand().direction(snowball.getVelocity().add(new Vector(0.0D, 0.15D, 0.0D)));
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.blizzard.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.blizzard.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.cooldown_reduction_per_level");
            BASE_MAX_CHARGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.blizzard.base_max_charge");
            MAX_CHARGE_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.blizzard.max_charge_reduction_per_level");
            BASE_MAX_CHARGE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.base_max_charge_duration");
            MAX_CHARGE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.max_charge_duration_increase_per_level");
            BASE_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.base_launch_strength");
            LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.launch_strength_increase_per_level");
            BASE_IMPACT_LAUNCH_STRENGTH_MODIFIER = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.base_impact_launch_strength_modifier");
            IMPACT_LAUNCH_STRENGTH_MODIFIER_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.blizzard.impact_launch_strength_modifier_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

}