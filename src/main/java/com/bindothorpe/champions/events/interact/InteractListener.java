package com.bindothorpe.champions.events.interact;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;

public class InteractListener implements Listener {

    private final PluginManager pluginManager;
    private final DomainController domainController;

    public InteractListener(DomainController domainController) {
        this.pluginManager = Bukkit.getPluginManager();
        this.domainController = domainController;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        boolean hasBuild = domainController.getPlayerManager().hasBuildSelected(event.getPlayer().getUniqueId());
        if(!hasBuild) return;

        if(event.getAction().isRightClick() && event.getHand() == EquipmentSlot.HAND) {
            pluginManager.callEvent(new PlayerRightClickEvent(event.getPlayer()));
        } else if (event.getAction().isLeftClick() && event.getHand() == EquipmentSlot.HAND) {
            pluginManager.callEvent(new PlayerLeftClickEvent(event.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        boolean hasBuild = domainController.getPlayerManager().hasBuildSelected(event.getPlayer().getUniqueId());

        if(!hasBuild) return;

        event.setCancelled(true);
        pluginManager.callEvent(new PlayerDropItemWrapperEvent(event.getPlayer(), event.getItemDrop().getItemStack()));
    }
}
