package com.bindothorpe.champions.domain.customItem;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomItemManager {

    private static CustomItemManager instance;
    private final DomainController dc;

    private final Map<CustomItemId, CustomItem> customItems = new HashMap<>();
    private final Map<UUID, List<CustomItemId>> playerItems = new HashMap<>();

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
        playerItems.computeIfAbsent(uuid, k -> new ArrayList<>());

        if (playerItems.get(uuid).size() >= 6)
            return;

        if (!doesUserHaveEnoughGold(uuid, customItemId))
            return;

        List<CustomItemId> itemsToRemove = getItemsToRemove(uuid, customItemId);
        int remainingCost = getRemainingCost(uuid, customItemId);

        for (CustomItemId itemToRemove : itemsToRemove) {
            removeItemFromUser(uuid, itemToRemove);
        }

        dc.reduceGold(uuid, remainingCost);

        playerItems.get(uuid).add(customItemId);
        customItems.get(customItemId).addUser(uuid);

        for (EntityStatus status : customItems.get(customItemId).getStatuses()) {
            int itemCount = Collections.frequency(playerItems.get(uuid), customItemId);

            dc.addStatusToEntity(uuid, status.multiplyValue(itemCount));
            dc.updateEntityStatus(uuid, status.getType());
        }

        updatePlayerInventory(uuid);
        System.out.println("Cooldown reduction: " + dc.getMultiplicationEntityStatusValue(uuid, EntityStatusType.COOLDOWN_REDUCTION));
    }

    private boolean doesUserHaveEnoughGold(UUID uuid, CustomItemId customItemId) {
        return getRemainingCost(uuid, customItemId) <= dc.getGold(uuid);
    }

    public void removeItemFromUser(UUID uuid, CustomItemId customItemId, boolean updateInventory) {
        playerItems.computeIfAbsent(uuid, k -> new ArrayList<>());
        playerItems.get(uuid).remove(customItemId);
        customItems.get(customItemId).removeUser(uuid);

        for (EntityStatus status : customItems.get(customItemId).getStatuses()) {
            dc.removeStatusFromEntity(uuid, status.getType(), status.getSource());
            dc.updateEntityStatus(uuid, status.getType());
        }

        if (updateInventory)
            updatePlayerInventory(uuid);

    }

    public void removeItemFromUser(UUID uuid, CustomItemId customItemId) {
        removeItemFromUser(uuid, customItemId, false);
    }


    private void updatePlayerInventory(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        for (int i = 0; i < 6; i++) {
            CustomItemId id = null;
            try {
                id = playerItems.get(uuid).get(i);
            } catch (IndexOutOfBoundsException ignored) {
            }

            CustomItem item = customItems.get(id);
            ItemStack itemStack = null;

            if (item != null) {
                itemStack = item.getItem(uuid);

            }


            player.getInventory().setItem(3 + i, itemStack);
        }
    }

    private List<CustomItemId> getAllSubItemsRecursively(CustomItemId customItemId) {
        List<CustomItemId> subItems = new ArrayList<>();
        CustomItem item = customItems.get(customItemId);

        if (item == null)
            return subItems;

        subItems.add(customItemId);

        for (CustomItemId subItem : item.getSubItems()) {
            subItems.addAll(getAllSubItemsRecursively(subItem));
        }

        return subItems;
    }

    private List<CustomItemId> getAllSubItems(CustomItemId customItemId) {
        List<CustomItemId> subItems = getAllSubItemsRecursively(customItemId);
        subItems.remove(customItemId);
        return subItems;
    }

    private List<CustomItemId> getAllMissingSubItems(UUID uuid, CustomItemId customItemId) {
        List<CustomItemId> subItemsRequired = getAllSubItems(customItemId);
        List<CustomItemId> subItemsOwned = new ArrayList<>(playerItems.get(uuid));

        //Loop through all the items required
        for (CustomItemId id : getAllSubItems(customItemId)) {

            //Check if the player has the item
            if (subItemsOwned.contains(id)) {

                //If the player has the item, remove it, and all sub items, from the list of required items
                List<CustomItemId> subItems = getAllSubItems(id);

                //Remove all sub items from the list of required items and owned items
                for (CustomItemId subItem : subItems) {
                    subItemsOwned.remove(subItem);
                    subItemsRequired.remove(subItem);
                }

                //Remove the item itself from the list of required items
                subItemsOwned.remove(id);

                //Remove the item itself from the list of required items
                subItemsRequired.remove(id);
            }
        }

        return subItemsRequired;
    }

    public List<CustomItemId> getItemsToRemove(UUID uuid, CustomItemId customItemId) {
        List<CustomItemId> subItemsRequired = getAllSubItems(customItemId);
        List<CustomItemId> subItemsOwned = new ArrayList<>(playerItems.get(uuid));
        List<CustomItemId> itemsToRemove = new ArrayList<>();

        //Loop through all the items required
        for (CustomItemId id : getAllSubItems(customItemId)) {

            //Check if the player has the item
            if (subItemsOwned.contains(id)) {

                //Get all sub items of the item
                List<CustomItemId> subItems = getAllSubItems(id);

                //Add the item to the list of items to remove
                itemsToRemove.add(id);

                //Remove the item itself from the items owned
                subItemsOwned.remove(id);

                //Remove the item itself from the list of required items
                subItemsRequired.remove(id);

                //Loop through all sub items
                for (CustomItemId subItem : subItems) {

                    //Remove the sub items from the items required
                    subItemsRequired.remove(subItem);
                }

            }
        }

        return itemsToRemove;
    }

    public int getRemainingCost(UUID uuid, CustomItemId customItemId) {
        List<CustomItemId> requiredItems = getAllMissingSubItems(uuid, customItemId);
        requiredItems.add(customItemId);

        return requiredItems.stream().map(customItems::get).map(CustomItem::getUpgradePrice).reduce(0, Integer::sum);
    }

    public boolean doesUserHaveItem(UUID uuid, CustomItemId customItemId) {
        playerItems.computeIfAbsent(uuid, k -> new ArrayList<>());
        return playerItems.get(uuid).contains(customItemId);
    }

    public int getItemCount(UUID uuid, CustomItemId id) {
        playerItems.computeIfAbsent(uuid, k -> new ArrayList<>());
        return Collections.frequency(playerItems.get(uuid), id);
    }


    public void registerItem(CustomItem customItem) {
        if (customItem == null)
            throw new IllegalArgumentException("Custom item cannot be null");
        if (customItems.containsKey(customItem.getId()))
            throw new IllegalArgumentException("Custom item already registered");
        if (customItem.getUpgradePrice() == 0)
            throw new IllegalArgumentException("Custom item must have an upgrade price");
        if (customItem.getSubItems().contains(customItem.getId()))
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
        if (item == null)
            return 0;

        return item.getTotalPrice();
    }

    public int getTier(CustomItemId itemId) {
        CustomItem item = customItems.get(itemId);
        if (item == null)
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
