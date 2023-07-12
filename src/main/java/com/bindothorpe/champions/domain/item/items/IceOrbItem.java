package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.util.ShapeUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class IceOrbItem extends GameItem {
    private final double icePrisonDuration;
    private boolean collisionFlag = false;
    private Entity collidedEntity;
    public IceOrbItem(DomainController dc, double duration, double icePrisonDuration, Entity owner) {
        super(dc, Material.ICE, duration, owner, 0.5, -1);
        this.icePrisonDuration = icePrisonDuration;
    }

    @Override
    public void onUpdate() {
        getLocation().getWorld().spawnParticle(Particle.SNOWFLAKE, getLocation(), 1, 0, 0, 0, 0, null, true);
    }

    @Override
    public void onCollide(Entity entity) {
        collisionFlag = true;
        collidedEntity = entity;
        dc.getGameItemManager().despawnItem(getId());
    }

    @Override
    public void onCollideWithBlock(Block block) {

    }

    @Override
    public void onDespawn() {
        List<Vector> vectors = ShapeUtil.sphere(5).stream().sorted((v1, v2) -> v2.getBlockY() - v1.getBlockY()).collect(Collectors.toList());

        Location loc = collisionFlag ? collidedEntity.getLocation() : getLocation();

        for(int i = 0; i < vectors.size(); i++) {
            Vector v = vectors.get(i);
            dc.getTemporaryBlockManager().spawnTemporaryBlock(loc.clone().add(v), Material.ICE, icePrisonDuration + (i / 200D));
        }
    }
}
