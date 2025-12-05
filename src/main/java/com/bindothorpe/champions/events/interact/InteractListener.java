package com.bindothorpe.champions.events.interact;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class InteractListener implements Listener {

    private final PluginManager pluginManager;
    private final DomainController domainController;

    public InteractListener(DomainController domainController) {
        this.pluginManager = Bukkit.getPluginManager();
        this.domainController = domainController;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(!hasBuildEquipped(event.getPlayer())) return;

        if(event.getAction().isRightClick() && event.getHand() == EquipmentSlot.HAND) {
            pluginManager.callEvent(new PlayerRightClickEvent(event.getPlayer()));
        } else if (event.getAction().isLeftClick() && event.getHand() == EquipmentSlot.HAND) {
            pluginManager.callEvent(new PlayerLeftClickEvent(event.getPlayer()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if(!hasBuildEquipped(player)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(!hasBuildEquipped(event.getPlayer())) return;

        event.setCancelled(true);
        pluginManager.callEvent(new PlayerDropItemWrapperEvent(event.getPlayer(), event.getItemDrop().getItemStack()));
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if(!hasBuildEquipped(player)) return;

        event.setKeepInventory(true);
        event.getDrops().clear();
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();

        if(!hasBuildEquipped(player)) return;

        event.setCancelled(true);

    }

    private boolean hasBuildEquipped(@NotNull Player player) {
        return domainController.getPlayerManager().hasBuildSelected(player.getUniqueId());
    }
}
