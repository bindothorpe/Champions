package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.MobilityUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FleshHookItem extends GameItem {

    private static final double COLLISION_RADIUS = 1.0;
    private final double collisionDamage;
    private final double collisionPullForce;
    private boolean flaggedForRemoval = false;

    public FleshHookItem(DomainController dc, Entity owner, double collisionDamage, double collisionPullForce) {
        super(dc, Material.TRIPWIRE_HOOK, -1, owner, COLLISION_RADIUS, 0.15, BlockCollisionMode.TOP_ONLY);
        this.collisionDamage = collisionDamage;
        this.collisionPullForce = collisionPullForce;
    }

    @Override
    public void onTickUpdate() {
        getLocation().getWorld().spawnParticle(Particle.CRIT, getLocation().clone().add(0, 0.4, 0), 1, 0, 0, 0, 0, null, true);

        //Play sound
        dc.getSoundManager().playSound(getLocation(), CustomSound.SKILL_FLESH_HOOK_AMBIENT);
    }

    @Override
    public void onRapidUpdate() {
    }

    @Override
    public void onCollide(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if(!(getOwner() instanceof LivingEntity owner)) {
            remove();
            return;
        }

        if(!dc.getTeamManager().areEntitiesOnDifferentTeams(owner, livingEntity))
            return;


        //Damage the enemy
        CustomDamageEvent customDamageEvent = CustomDamageEvent.getBuilder()
                .setDamager(owner)
                .setDamagee(livingEntity)
                .setDamage(collisionDamage)
                .setCause(CustomDamageEvent.DamageCause.SKILL_PROJECTILE)
                .setForceMultiplier(0)
                .setCauseDisplayName(dc.getSkillManager().getSkillName(SkillId.FLESH_HOOK))
                .setSendSkillHitToReceiver(true)
                .setSendSkillHitToCaster(true)
                .build();


        customDamageEvent.callEvent();


        if(customDamageEvent.isCancelled())
            return;

        new CustomDamageCommand(dc, customDamageEvent).execute();

        //Pull entity towards owner
        MobilityUtil.launch(livingEntity,
                MobilityUtil.directionTo(livingEntity.getLocation(), getOwner().getLocation()),
                collisionPullForce,
                false,
                0.0D,
                0.8D,
                1.5D,
                true
                );

        dc.getSoundManager().playSound(getLocation(), CustomSound.SKILL_FLESH_HOOK_THROW);
        remove();
    }

    @Override
    public void onCollideWithBlock(Block block) {
        if (flaggedForRemoval) return;
        flaggedForRemoval = true;
        new Timer(dc.getPlugin(), 0.2, this::remove).start();
    }

    @Override
    public void onDespawn() {

    }
}
