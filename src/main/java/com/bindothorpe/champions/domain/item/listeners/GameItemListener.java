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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameItemListener implements Listener {
    private final static double COLLISION_RADIUS = 0.5;
    private DomainController dc;

    public GameItemListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if(dc.getGameItemManager().isGameItem(event.getEntity())) event.setCancelled(true);
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
    public void onSlowUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.RAPID))
            return;

        dc.getGameItemManager().getGameItems().forEach(GameItem::onRapidUpdate);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        dc.getGameItemManager().getGameItems().forEach(GameItem::onTickUpdate);
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
            if(blockCollisionRadius != -1) {
                collidingBlocks.addAll(BlockUtil.getNearbyBlocks(gameItem.getLocation(), blockCollisionRadius));

                // Filter blocks based on collision mode
                if(gameItem.getBlockCollisionMode() == GameItem.BlockCollisionMode.TOP_ONLY) {
                    collidingBlocks = filterTopSurfaceOnly(gameItem.getLocation(), collidingBlocks);
                }
            }

            if(!collidingBlocks.isEmpty())
                Bukkit.getPluginManager().callEvent(new GameItemCollideWithBlockEvent(dc, gameItem, collidingBlocks.get(0)));
        });
    }

    /**
     * Filters blocks to only include those that are below the item and have air above them (top surface)
     */
    private List<Block> filterTopSurfaceOnly(Location itemLocation, List<Block> blocks) {
        return blocks.stream()
                .filter(block -> {
                    // Check if block is below the item (item's Y is higher than block's top surface)
                    double blockTopY = block.getY() + 1.0;
                    boolean isBelow = itemLocation.getY() >= blockTopY - 0.5; // Small tolerance for edge cases

                    // Check if the block is solid and has air or non-solid block above it
                    boolean isSolid = block.getType().isSolid();
                    Block blockAbove = block.getRelative(0, 1, 0);
                    boolean hasAirAbove = !blockAbove.getType().isSolid() || blockAbove.getType() == Material.AIR;

                    return isBelow && isSolid && hasAirAbove;
                })
                .collect(Collectors.toList());
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