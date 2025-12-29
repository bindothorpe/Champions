package com.bindothorpe.champions.gui.items.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.gui.map.details.ListSpawnPointsMapGui;
import com.bindothorpe.champions.util.ComponentUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuSpawnPointsItem extends GuiItem {

    private final DomainController dc;
    private final GameMap gameMap;

    public MenuSpawnPointsItem(DomainController dc, GameMap gameMap) {
        super(new ItemStack(Material.PLAYER_HEAD));
        this.dc = dc;
        this.gameMap = gameMap;
        setItem(getDisplayItem());
        setAction(this::handleClick);
    }

    private void handleClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player player)) return;

        if(!event.isLeftClick()) return;

        new ListSpawnPointsMapGui(player.getUniqueId(), dc, gameMap).open();
    }

    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Spawn Points")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(String.format("%d spawn points", gameMap.getGameObjectsOfType(GameObjectType.SPAWN_POINT).size()), NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.leftClick(true).append(Component.text("View Spawn points.", NamedTextColor.GRAY)),
                30
        ));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
