package com.bindothorpe.champions.domain.customItem;

import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class CustomItem implements Listener {

    private static Set<UUID> users = new HashSet<>();
    private final CustomItemManager manager;
    private final CustomItemId id;
    private final String name;
    private final int upgradePrice;
    private final List<CustomItemId> subItems;

    public CustomItem(CustomItemManager manager, CustomItemId id, String name, int upgradePrice, List<CustomItemId> subItems) {
        this.manager = manager;
        this.id = id;
        this.name = name;
        this.upgradePrice = upgradePrice;
        this.subItems = subItems;
    }

    public CustomItem(CustomItemManager manager, CustomItemId id, String name, int upgradePrice) {
        this(manager, id, name, upgradePrice, List.of());
    }

    public void addUser(UUID uuid) {
        users.add(uuid);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public CustomItemId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getUpgradePrice() {
        return upgradePrice;
    }

    public int getTotalPrice() {
        return subItems.stream().map(manager::getTotalPrice).reduce(upgradePrice, Integer::sum);
    }

    public List<CustomItemId> getSubItems() {
        return subItems;
    }

    public int getTier() {
        int tier = 0;

        for (CustomItemId subItem : subItems) {
            tier = Math.max(tier, manager.getTier(subItem));
        }

        return tier + 1;
    }

    public abstract ItemStack getItem();

    protected abstract List<Component> getLore();

}
