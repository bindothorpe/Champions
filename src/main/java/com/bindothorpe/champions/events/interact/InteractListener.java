package com.bindothorpe.champions.events.interact;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;

public class InteractListener implements Listener {

    private final PluginManager pluginManager;

    public InteractListener() {
        this.pluginManager = Bukkit.getPluginManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getAction().isRightClick() && event.getHand().equals(EquipmentSlot.HAND)) {
            pluginManager.callEvent(new PlayerRightClickEvent(event.getPlayer()));
        } else if (event.getAction().isLeftClick() && event.getHand().equals(EquipmentSlot.HAND)) {
            pluginManager.callEvent(new PlayerLeftClickEvent(event.getPlayer()));
        }
    }
}
