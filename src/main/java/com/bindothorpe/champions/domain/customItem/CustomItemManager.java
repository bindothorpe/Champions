package com.bindothorpe.champions.domain.customItem;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Bukkit;

import java.util.*;

public class CustomItemManager {

    private static CustomItemManager instance;
    private final DomainController dc;

    private final Map<CustomItemId, CustomItem> customItems = new HashMap<>();
    private final Map<UUID, Set<CustomItemId>> playerItems = new HashMap<>();

    private CustomItemManager(DomainController dc) {
        this.dc = dc;
    }

    public static CustomItemManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new CustomItemManager(dc);
        }
        return instance;
    }

    public void addItemToUser(UUID uuid, CustomItemId customItemId) {
        playerItems.computeIfAbsent(uuid, k -> new HashSet<>());
        playerItems.get(uuid).add(customItemId);
        customItems.get(customItemId).addUser(uuid);
    }

    public void removeItemFromUser(UUID uuid, CustomItemId customItemId) {
        playerItems.computeIfAbsent(uuid, k -> new HashSet<>());
        playerItems.get(uuid).remove(customItemId);
        customItems.get(customItemId).removeUser(uuid);
    }

    public boolean doesUserHaveItem(UUID uuid, CustomItemId customItemId) {
        playerItems.computeIfAbsent(uuid, k -> new HashSet<>());
        return playerItems.get(uuid).contains(customItemId);
    }


    public void registerItem(CustomItem customItem) {
        if(customItem == null)
            throw new IllegalArgumentException("Custom item cannot be null");
        if(customItems.containsKey(customItem.getId()))
            throw new IllegalArgumentException("Custom item already registered");
        if(customItem.getUpgradePrice() == 0)
            throw new IllegalArgumentException("Custom item must have an upgrade price");
        if(customItem.getSubItems().contains(customItem.getId()))
            throw new IllegalArgumentException("Custom item cannot be a sub item of itself");

//        if(customItem.getSubItems().stream().anyMatch(subItem -> !customItems.containsKey(subItem)))
//            throw new IllegalArgumentException("Custom item cannot have a sub item that is not registered");
//        if(customItem.getSubItems().stream().anyMatch(subItem -> customItem.getSubItems().contains(subItem)))
//            throw new IllegalArgumentException("Custom item cannot have a sub item that is also a sub item of itself");

        customItems.put(customItem.getId(), customItem);
        Bukkit.getPluginManager().registerEvents(customItem, dc.getPlugin());
    }

    public int getTotalPrice(CustomItemId customItemId) {
        CustomItem item = customItems.get(customItemId);
        if(item == null)
            return 0;

        return item.getTotalPrice();
    }

    public int getTier(CustomItemId itemId) {
        CustomItem item = customItems.get(itemId);
        if(item == null)
            return 0;

        return item.getTier();
    }

    public CustomItem getCustomItem(CustomItemId customItemId) {
        return customItems.get(customItemId);
    }

    public List<CustomItemId> getSubItems(CustomItemId customItemId) {
        return customItems.get(customItemId).getSubItems();
    }
}