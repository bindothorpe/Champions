package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
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
    public void onUpdate() {
        if (!sticky)
            getLocation().getWorld().spawnParticle(Particle.SMALL_FLAME, getLocation().clone().add(0, 0.4, 0), 1, 0, 0, 0, 0, null, true);
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
    }

    @Override
    public void onDespawn() {
        getLocation().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, getLocation(), 1, 0, 0, 0, 0, null, true);
        Set<Entity> nearby = new HashSet<>(getLocation().getWorld().getNearbyEntities(getLocation(), EXPLOSION_RADIUS, EXPLOSION_RADIUS, EXPLOSION_RADIUS).stream().filter(e -> e instanceof LivingEntity).collect(Collectors.toSet()));

        sticky = true;

        if(nearby.isEmpty())
            return;

        EntityStatus status = new EntityStatus(EntityStatusType.KNOCKBACK_DONE, EXPLOSION_KNOCKBACK, -1, false, false, this);
        dc.addStatusToEntity(getOwner().getUniqueId(), status);
        for(Entity e : nearby) {
            CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, (LivingEntity) e, getOwner(), explosionDamage, getLocation(), CustomDamageSource.SKILL);

            if(customDamageEvent.isCancelled())
                return;

            if(!customDamageEvent.getHitBy().equals(getOwner())) {
                customDamageEvent.getEntity().setHealth(Math.max(0, customDamageEvent.getEntity().getHealth() - customDamageEvent.getFinalDamage()));
            }
            Vector direction = customDamageEvent.getEntity().getLocation().toVector().subtract(getLocation().toVector()).normalize();
            if(!e.equals(getOwner())) {
                direction = customDamageEvent.getKnockbackDirection();
            }
            customDamageEvent.getEntity().setVelocity(customDamageEvent.getEntity().getVelocity().add(direction.multiply(customDamageEvent.getFinalKnockback())));

        }
        dc.removeStatusFromEntity(getOwner().getUniqueId(), EntityStatusType.KNOCKBACK_DONE, this);
    }
}
