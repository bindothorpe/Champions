package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ExplosiveItem extends GameItem {
    private boolean sticky = false;
    private static final double EXPLOSION_RADIUS = 2.5;
    private static final double EXPLOSION_KNOCKBACK = 0.9;

    private final double explosionDamage;

    public ExplosiveItem(DomainController dc, Entity owner, double explosionDamage) {
        super(dc, Material.TNT, -1, owner, -1, 0.15);
        this.explosionDamage = explosionDamage;
    }

    @Override
    public void onTickUpdate() {
        if (!sticky) {
            getLocation().getWorld().spawnParticle(Particle.SMALL_FLAME, getLocation().clone().add(0, 0.4, 0), 1, 0, 0, 0, 0, null, true);

        }

    }

    @Override
    public void onRapidUpdate() {
        if (!sticky) {
            dc.getSoundManager().playSound(getLocation(), CustomSound.SKILL_EXPLOSION_BOMB_ORB_AMBIENT);
        }
    }

    @Override
    public void onCollide(Entity entity) {
        remove();
    }

    @Override
    public void onCollideWithBlock(Block block) {
        sticky = true;
        setEntityCollisionRadius(0.4);
        setBlockCollisionRadius(-1);
        getItem().setGravity(false);
        getItem().setVelocity(new Vector(0, 0, 0));
        dc.getSoundManager().playSound(getLocation(), CustomSound.SKILL_EXPLOSION_BOMB_ORB_STICK);
    }

    @Override
    public void onDespawn() {

        if(!(getOwner() instanceof LivingEntity owner)) return;

        getLocation().getWorld().spawnParticle(Particle.EXPLOSION, getLocation(), 1, 0, 0, 0, 0, null, true);
        Set<Entity> nearby = new HashSet<>(getLocation().getWorld().getNearbyEntities(getLocation(), EXPLOSION_RADIUS, EXPLOSION_RADIUS, EXPLOSION_RADIUS));

        sticky = true;
        dc.getSoundManager().playSound(getLocation(), CustomSound.SKILL_EXPLOSION_BOMB_EXPLODE);

        if(nearby.isEmpty())
            return;

        for(Entity e : nearby) {

            if(!(e instanceof LivingEntity damagee)) continue;

            CustomDamageEvent customDamageEvent = CustomDamageEvent.getBuilder()
                    .setDamager(owner)
                    .setDamagee(damagee)
                    .setDamage(explosionDamage)
                    .setForceMultiplier(1 + EXPLOSION_KNOCKBACK)
                    .setLocation(getLocation())
                    .setCause(CustomDamageEvent.DamageCause.SKILL)
                    .setCauseDisplayName("TODO: Add optional Skill ID to Game Items")
                    .setSendSkillHitToCaster(true)
                    .setSendSkillHitToReceiver(true)
                    .build();

            if(!dc.getTeamManager().areEntitiesOnDifferentTeams(e, getOwner())) {
                if(e.equals(getOwner())) {
                    customDamageEvent.setDamage(0);
                } else {
                    customDamageEvent.setCancelled(true);
                    continue;
                }
            }


            customDamageEvent.callEvent();

            if(customDamageEvent.isCancelled())
                return;

            new CustomDamageCommand(dc, customDamageEvent).execute();
        }
    }
}
