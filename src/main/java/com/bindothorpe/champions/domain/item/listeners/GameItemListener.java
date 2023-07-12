package com.bindothorpe.champions.domain.item.listeners;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.events.GameItemCollideWithBlockEvent;
import com.bindothorpe.champions.domain.item.events.GameItemCollideWithEntityEvent;
import com.bindothorpe.champions.domain.item.events.GameItemPickupEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.BlockUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameItemListener implements Listener {
    private final static double COLLISION_RADIUS = 0.5;
    private DomainController dc;

    public GameItemListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {

        if (!dc.getGameItemManager().isGameItem(event.getItem()))
            return;

        GameItem item = dc.getGameItemManager().getGameItem(event.getItem());
        event.setCancelled(true);

        if(event.getEntity().equals(item.getOwner()))
            return;

        Bukkit.getPluginManager().callEvent(new GameItemPickupEvent(dc, item, event.getEntity()));
        dc.getGameItemManager().despawnItem(item.getId());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        dc.getGameItemManager().getGameItems().forEach(GameItem::onUpdate);
        dc.getGameItemManager().getGameItems().forEach(gameItem -> {
            double entityCollisionRadius = gameItem.getEntityCollisionRadius();
            double blockCollisionRadius = gameItem.getBlockCollisionRadius();

            List<Entity> colliding = new ArrayList<>();
            if(entityCollisionRadius != -1) {
                colliding.addAll(gameItem.getLocation().getNearbyEntities(entityCollisionRadius, entityCollisionRadius, entityCollisionRadius).stream().filter(entity -> entity instanceof LivingEntity).collect(Collectors.toList()));
                colliding.remove(gameItem.getOwner());
            }

            if(!colliding.isEmpty())
                Bukkit.getPluginManager().callEvent(new GameItemCollideWithEntityEvent(dc, gameItem, colliding.get(0)));


            List<Block> collidingBlocks = new ArrayList<>();
            if(blockCollisionRadius != -1)
                collidingBlocks.addAll(BlockUtil.getNearbyBlocks(gameItem.getLocation(), blockCollisionRadius));

            if(!collidingBlocks.isEmpty())
                Bukkit.getPluginManager().callEvent(new GameItemCollideWithBlockEvent(dc, gameItem, collidingBlocks.get(0)));
        });
    }

    @EventHandler
    public void onCollide(GameItemCollideWithEntityEvent event) {
        event.getGameItem().onCollide(event.getEntity());
    }

    @EventHandler
    public void onCollideWithBlock(GameItemCollideWithBlockEvent event) {
        event.getGameItem().onCollideWithBlock(event.getBlock());
    }
}
