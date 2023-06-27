package com.bindothorpe.champions.domain.customItem;

import org.bukkit.event.Listener;

import java.util.Set;

public abstract class CustomItem implements Listener {

    private final CustomItemId id;
    private final String name;
    private final int price;
    private final Set<CustomItemId> components;

    public CustomItem(CustomItemId id, String name, int price, Set<CustomItemId> components) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.components = components;
    }
}
