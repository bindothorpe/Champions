package com.bindothorpe.champions.domain.item;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;

public abstract class GameItem {

    private Item item;
    private UUID id;

    protected final DomainController dc;
    private final Material material;

    private final double duration;
    private final Entity owner;

    private double entityCollisionRadius = -1;
    private double blockCollisionRadius = -1;

    public GameItem(DomainController dc, Material material, double duration, Entity owner, double entityCollisionRadius, double blockCollisionRadius) {
        this.dc = dc;
        this.material = material;
        this.duration = duration;
        this.owner = owner;
        this.entityCollisionRadius = entityCollisionRadius;
        this.blockCollisionRadius = blockCollisionRadius;
    }

    public UUID spawn(Location startingLocation, Vector direction, double strength) {
        item = owner.getWorld().dropItem(startingLocation, new ItemStack(material));
        item.setVelocity(direction.multiply(strength));
        this.id = item.getUniqueId();
        return id;
    }


    public void remove() {
        onDespawn();
        item.remove();
    }

    public double getDuration() {
        return duration;
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return item.getLocation();
    }

    public abstract void onUpdate();

    public abstract void onCollide(Entity entity);
    public abstract void onCollideWithBlock(Block block);

    public abstract void onDespawn();

    public Item getItem() {
        return item;
    }

    public Entity getOwner() {
        return owner;
    }

    public double getEntityCollisionRadius() {
        return entityCollisionRadius;
    }

    public double getBlockCollisionRadius() {
        return blockCollisionRadius;
    }

    public void setEntityCollisionRadius(double entityCollisionRadius) {
        this.entityCollisionRadius = entityCollisionRadius;
    }

    public void setBlockCollisionRadius(double blockCollisionRadius) {
        this.blockCollisionRadius = blockCollisionRadius;
    }
}
