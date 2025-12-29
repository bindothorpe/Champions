package com.bindothorpe.champions.gui.items.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.dialogs.map.edit.SelectClassTypeDialog;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChampionSelectGameObject;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChestGameObject;
import com.bindothorpe.champions.domain.game.map.gameObjects.SpawnPointGameObject;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.gui.map.details.ListChampionSelectsMapGui;
import com.bindothorpe.champions.gui.map.details.ListChestsMapGui;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuChampionSelectsItem extends GuiItem {

    private final DomainController dc;
    private final boolean disableAndHideLeftClick;
    private final GameMap gameMap;
    private final Consumer<UUID> onAddChampionSelectConsumer;

    public MenuChampionSelectsItem(DomainController dc, boolean disableAndHideLeftClick, GameMap gameMap, Consumer<UUID> onAddChampionSelectConsumer) {
        super(new ItemStack(Material.ENCHANTING_TABLE));
        this.dc = dc;
        this.disableAndHideLeftClick = disableAndHideLeftClick;
        this.gameMap = gameMap;
        this.onAddChampionSelectConsumer = onAddChampionSelectConsumer;
        setAction(this::handleClick);
        setItem(getDisplayItem());
    }

    public MenuChampionSelectsItem(DomainController dc, GameMap gameMap, Consumer<UUID> onAddGemConsumer) {
        this(dc, false, gameMap, onAddGemConsumer);
    }


    @SuppressWarnings("UnstableApiUsage")
    private void handleClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player player)) return;

        if(event.isLeftClick() && !disableAndHideLeftClick) {
            new ListChampionSelectsMapGui(player.getUniqueId(), dc, gameMap).open();
            return;
        }

        if(event.isRightClick()) {

            if(gameMap.getGameObjectsOfType(GameObjectType.CHAMPION_SELECT).size() >= ListChampionSelectsMapGui.MAX_CHAMPION_SELECTS) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You heave reached the maximum chests amount.", NamedTextColor.GRAY));
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
                return;
            }


            if(GameMap.isOverlappingOtherGameObject(player.getLocation(), gameMap.getGameObjects())) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are too close to another game object.", NamedTextColor.GRAY));
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
                return;
            }


            player.showDialog(SelectClassTypeDialog.createActions((classType -> {
                gameMap.addGameObject(new ChampionSelectGameObject(
                        classType,
                        player.getFacing(),
                        player.getLocation().toBlockLocation().toVector()
                ));

                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You added a new Champion select game object!", NamedTextColor.GRAY));
                onAddChampionSelectConsumer.accept(player.getUniqueId());

            })));

        }
    }


    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Champion Select")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));


        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(String.format("%d champion selects", gameMap.getGameObjectsOfType(GameObjectType.CHAMPION_SELECT).size()), NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        if(!disableAndHideLeftClick)
            lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                    ComponentUtil.leftClick(true).append(Component.text("View Champion Select details.", NamedTextColor.GRAY)),
                    30
            ));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.rightClick(true).append(Component.text("Create Champion Select object.", NamedTextColor.GRAY)),
                30
        ));

        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
