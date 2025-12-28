package com.bindothorpe.champions.gui.items.map.edit;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChestGameObject;
import com.bindothorpe.champions.domain.game.map.gameObjects.GemGameObject;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.gui.map.details.ListChestsMapGui;
import com.bindothorpe.champions.gui.map.details.ListGemsMapGui;
import com.bindothorpe.champions.util.ComponentUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChestInstanceItem extends GuiItem {

    private final DomainController dc;
    private final ChestGameObject gameObject;
    private final GameMap gameMap;

    public ChestInstanceItem(DomainController dc, ChestGameObject gameObject, GameMap gameMap) {
        super(new ItemStack(Material.CHEST));
        this.dc = dc;
        this.gameObject = gameObject;
        this.gameMap = gameMap;
        setItem(getDisplayItem());
        setAction(this::handleClick);
    }

    private void handleClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player player)) return;

        if(event.isLeftClick()) {
            // Teleport to the location
            if(gameMap.getSlimeWorldInstance() == null) return;

            World world = gameMap.getSlimeWorldInstance().getBukkitWorld();
            player.teleport(gameObject.worldLocation().toLocation(world));
        } else if (event.isRightClick()) {
            // Remove object from gameMap
            gameMap.removeGameObject(gameObject);
            new ListChestsMapGui(player.getUniqueId(), dc, gameMap).open();
        }
        dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK);
    }

    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Chest")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Location:", NamedTextColor.WHITE));
        lore.add(Component.text(String.format("  x: %.1f", gameObject.worldLocation().getX()), NamedTextColor.GRAY));
        lore.add(Component.text(String.format("  y: %.1f", gameObject.worldLocation().getY()), NamedTextColor.GRAY));
        lore.add(Component.text(String.format("  z: %.1f", gameObject.worldLocation().getZ()), NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.add(ComponentUtil.leftClick(true).append(Component.text("to teleport.", NamedTextColor.GRAY)));
        lore.add(ComponentUtil.rightClick(true).append(Component.text("to delete.", NamedTextColor.GRAY)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
