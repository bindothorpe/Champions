package com.bindothorpe.champions.domain.item;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.events.GameItemDespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class GameItemManager {

    private static GameItemManager instance;

    private final DomainController dc;

    private final Map<UUID, GameItem> gameItems = new HashMap<>();

    private GameItemManager(DomainController dc) {
        this.dc = dc;
    }

    public static GameItemManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new GameItemManager(dc);
        }
        return instance;
    }

    public void spawnGameItem(GameItem gameItem, Location startingLocation, Vector direction, double strength) {
        UUID uuid = gameItem.spawn(startingLocation, direction, strength);
        gameItems.put(uuid, gameItem);

        new BukkitRunnable() {
            @Override
            public void run() {
                despawnItem(uuid);
            }
        }.runTaskLater(dc.getPlugin(), (long) (gameItem.getDuration() * 20));
    }

    public boolean isGameItem(Item item) {
        return gameItems.containsKey(item.getUniqueId());
    }

    public void despawnItem(UUID uuid) {
        GameItem gameItem = gameItems.get(uuid);
        if (gameItem != null) {
            gameItem.remove();
            gameItems.remove(uuid);
            Bukkit.getPluginManager().callEvent(new GameItemDespawnEvent(dc, gameItem));
        }
    }

    public GameItem getGameItem(Item item) {
        return gameItems.get(item.getUniqueId());
    }

    public Set<GameItem> getGameItems() {
        return new HashSet<>(gameItems.values());
    }

}
