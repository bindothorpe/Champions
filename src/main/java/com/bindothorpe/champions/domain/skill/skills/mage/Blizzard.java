package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ChargeSkill;
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

public class Blizzard extends ChargeSkill {

    private static final Random random = new Random();

    public Blizzard(DomainController dc) {
        super(dc, SkillId.BLIZZARD, SkillType.SWORD, ClassType.MAGE, "Blizzard", List.of(10D, 8D, 6D), 3, 1, List.of(1, 1, 1), List.of(2.5D, 3D, 3.5D));
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

            CustomDamageEvent.addCustomDamageSourceData(dc, snowball, CustomDamageSource.SKILL_PROJECTILE);
            CustomDamageEvent.addSkillIdData(dc, snowball, getId());

            double x = 0.2D - random.nextInt(40) / 100.0D;
            double y = random.nextInt(20) / 100.0D;
            double z = 0.2D - random.nextInt(40) / 100.0D;
            snowball.setVelocity(player.getLocation().getDirection().add(new Vector(x, y, z)).multiply(2));

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

        event.getCommand().suppressHitSound();
        event.getCommand().damage(0);
        event.getCommand().force(snowball.getVelocity().multiply(0.05D).length());
        event.getCommand().direction(snowball.getVelocity().add(new Vector(0.0D, 0.15D, 0.0D)));
    }

}
