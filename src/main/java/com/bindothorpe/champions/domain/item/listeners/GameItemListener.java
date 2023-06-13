package com.bindothorpe.champions.domain.item.listeners;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.events.GameItemCollideWithEntityEvent;
import com.bindothorpe.champions.domain.item.events.GameItemPickupEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
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

        if (!dc.isGameItem(event.getItem()))
            return;

        GameItem item = dc.getGameItem(event.getItem());

        event.setCancelled(true);

        Bukkit.getPluginManager().callEvent(new GameItemPickupEvent(dc, item, event.getEntity()));
        dc.despawnItem(item.getId());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        dc.getGameItems().forEach(GameItem::onUpdate);
        dc.getGameItems().forEach(gameItem -> {
            List<Entity> colliding = new ArrayList<>(gameItem.getLocation().getNearbyEntities(COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS).stream().filter(entity -> entity instanceof LivingEntity).collect(Collectors.toList()));
            colliding.remove(gameItem.getOwner());

            if(colliding.isEmpty())
                return;

            Bukkit.getPluginManager().callEvent(new GameItemCollideWithEntityEvent(dc, gameItem, colliding.get(0)));
        });
    }

    @EventHandler
    public void onCollide(GameItemCollideWithEntityEvent event) {
        event.getGameItem().onCollide(event.getEntity());
    }
}
