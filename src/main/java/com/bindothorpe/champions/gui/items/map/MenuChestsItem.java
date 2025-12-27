package com.bindothorpe.champions.gui.items.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuChestsItem extends GuiItem {

    private final DomainController dc;
    private final GameMap gameMap;

    public MenuChestsItem(DomainController dc, GameMap gameMap) {
        super(new ItemStack(Material.CHEST));
        this.dc = dc;
        this.gameMap = gameMap;
        setItem(getDisplayItem());
    }

    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Chests")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        item.setItemMeta(meta);
        return item;
    }
}
