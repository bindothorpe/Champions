package com.bindothorpe.champions.gui.items.map.edit.spawnPoint;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChestGameObject;
import com.bindothorpe.champions.domain.game.map.gameObjects.SpawnPointGameObject;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.gui.map.details.ListChestsMapGui;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.EntityUtil;
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
import java.util.stream.Collectors;

public class SpawnPointTeamItem extends GuiItem {

    private final DomainController dc;
    private final TeamColor teamColor;
    private final GameMap gameMap;
    private final Consumer<UUID> onAddSpawnPointConsumer;

    public SpawnPointTeamItem(DomainController dc, TeamColor teamColor, GameMap gameMap, Consumer<UUID> onAddSpawnPointConsumer) {
        super(new ItemStack(teamColor.equals(TeamColor.BLUE) ? Material.BLUE_CONCRETE : Material.RED_CONCRETE));
        this.dc = dc;
        this.teamColor = teamColor;
        this.gameMap = gameMap;
        this.onAddSpawnPointConsumer = onAddSpawnPointConsumer;
        setAction(this::handleClick);
        setItem(getDisplayItem());
    }


    private void handleClick(InventoryClickEvent event) {

        if(!(event.getWhoClicked() instanceof Player player)) return;

        if(event.isRightClick()) {

            if(gameMap.getGameObjectsOfType(GameObjectType.SPAWN_POINT).stream().filter(gameObject -> ((SpawnPointGameObject) gameObject).team().equals(teamColor)).collect(Collectors.toSet()).size() >= ListChestsMapGui.MAX_CHESTS) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You heave reached the maximum spawn point amount.", NamedTextColor.GRAY));
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
                return;
            }


            if(GameMap.isOverlappingOtherGameObject(player.getLocation(), gameMap.getGameObjects())) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are too close to another game object.", NamedTextColor.GRAY));
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
                return;
            }

            gameMap.addGameObject(new SpawnPointGameObject(
                    teamColor,
                    player.getFacing(),
                    player.getLocation().toBlockLocation().toVector()
            ));

            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You added a new Spawn point game object!", NamedTextColor.GRAY));
            dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK);
            onAddSpawnPointConsumer.accept(player.getUniqueId());
        }
    }

    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Spawn Points")
                .color(teamColor.getTextColor())
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));


        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(String.format("%d spawn points", gameMap.getGameObjectsOfType(GameObjectType.SPAWN_POINT).stream().filter(gameObject -> ((SpawnPointGameObject) gameObject).team().equals(teamColor)).toList().size()), NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.rightClick(true).append(Component.text("Create Spawn point object.", NamedTextColor.GRAY)),
                30
        ));

        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
