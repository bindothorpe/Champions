package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.game.map.PlayerStartEditingMapEvent;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameMapListener implements Listener {

    private final DomainController dc;

    public GameMapListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onPlayerStartEditingMap(PlayerStartEditingMapEvent event) {
        event.getPlayer().getInventory().clear();

        ItemStack compassItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = compassItem.getItemMeta();
        meta.displayName(Component.text("Map Edit Tool", NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true));
        compassItem.setItemMeta(meta);
        event.getPlayer().getInventory().setItem(8, compassItem);
        event.getPlayer().setGameMode(GameMode.CREATIVE);
        event.getPlayer().setFlying(true);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick()) return;
        if(!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NETHER_STAR)) return;

        GameMap gameMap = GameMapManager.getInstance(dc).getEditingMapForPlayer(event.getPlayer());
        if(gameMap == null) return;

        dc.getGuiManager().openEditMapGui(event.getPlayer().getUniqueId(), dc, gameMap);
    }
}
