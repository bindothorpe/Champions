package com.bindothorpe.champions.gui.items.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChestGameObject;
import com.bindothorpe.champions.domain.game.map.gameObjects.GemGameObject;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.gui.map.details.ListChestsMapGui;
import com.bindothorpe.champions.gui.map.details.ListGemsMapGui;
import com.bindothorpe.champions.util.ChatUtil;
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
import java.util.UUID;
import java.util.function.Consumer;

public class MenuChestsItem extends GuiItem {

    private final DomainController dc;
    private final boolean disableAndHideLeftClick;
    private final GameMap gameMap;
    private final Consumer<UUID> onAddChestConsumer;

    public MenuChestsItem(DomainController dc, boolean disableAndHideLeftClick, GameMap gameMap, Consumer<UUID> onAddChestConsumer) {
        super(new ItemStack(Material.CHEST));
        this.dc = dc;
        this.disableAndHideLeftClick = disableAndHideLeftClick;
        this.gameMap = gameMap;
        this.onAddChestConsumer = onAddChestConsumer;
        setAction(this::handleClick);
        setItem(getDisplayItem());
    }

    public MenuChestsItem(DomainController dc, GameMap gameMap, Consumer<UUID> onAddGemConsumer) {
        this(dc, false, gameMap, onAddGemConsumer);
    }

    private void handleClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player player)) return;

        if(event.isLeftClick() && !disableAndHideLeftClick) {
            new ListChestsMapGui(player.getUniqueId(), dc, gameMap).open();
            return;
        }

        if(event.isRightClick()) {

            if(gameMap.getGameObjectsOfType(GameObjectType.CHEST).size() >= ListChestsMapGui.MAX_CHESTS) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You heave reached the maximum chests amount.", NamedTextColor.GRAY));
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
                return;
            }


            if(GameMap.isOverlappingOtherGameObject(player.getLocation(), gameMap.getGameObjects())) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are too close to another game object.", NamedTextColor.GRAY));
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
                return;
            }

            gameMap.addGameObject(new ChestGameObject(
                    player.getLocation().toBlockLocation().toVector()
            ));

            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You added a new Chest game object!", NamedTextColor.GRAY));
            dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK);
            onAddChestConsumer.accept(player.getUniqueId());
        }
    }

    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Chests")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));


        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(String.format("%d chests", gameMap.getGameObjectsOfType(GameObjectType.CHEST).size()), NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        if(!disableAndHideLeftClick)
            lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                    ComponentUtil.leftClick(true).append(Component.text("View Chest details.", NamedTextColor.GRAY)),
                    30
            ));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.rightClick(true).append(Component.text("Create Chest object.", NamedTextColor.GRAY)),
                30
        ));

        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
