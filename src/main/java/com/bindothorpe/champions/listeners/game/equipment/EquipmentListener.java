package com.bindothorpe.champions.listeners.game.equipment;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class EquipmentListener implements Listener {

    private final DomainController dc;

    public EquipmentListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onPlayerItemHeldChange(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        Player player = event.getPlayer();

        if (item == null || (!ItemUtil.isSword(item.getType()))) {
            handleSelectOther(player);
        } else {
            handleSelectSword(player);
        }

    }

    private void handleSelectSword(Player player) {
        player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
    }

    private void handleSelectOther(Player player) {
        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    }
}
