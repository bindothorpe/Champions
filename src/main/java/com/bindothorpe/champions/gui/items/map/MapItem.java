package com.bindothorpe.champions.gui.items.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMapData;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapItem extends GuiItem {

    private final DomainController dc;
    private final String mapName;

    public MapItem(DomainController dc, String mapName) {
        super(new ItemStack(Material.FILLED_MAP));
        this.dc = dc;
        this.mapName = mapName;
        setItem(getDisplayItem());
        setAction(this::onClick);
    }

    private void onClick(InventoryClickEvent inventoryClickEvent) {
        if(inventoryClickEvent.isLeftClick()) {
//            dc.getGuiManager().openMapEditGui(inventoryClickEvent.getWhoClicked().getUniqueId(), mapName);
        } else if(inventoryClickEvent.isRightClick()) {
            Player player = Bukkit.getPlayer(inventoryClickEvent.getWhoClicked().getUniqueId());

            if(player == null) {
                return;
            }

            dc.getGameMapManager().loadMap(mapName);
            dc.getGameMapManager().teleportToMap(player, mapName);
        }
    }

    private ItemStack getDisplayItem() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(TextUtil.camelCasing(mapName))
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));


        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        if(mapData != null) {

            List<String> capturePoints = mapData.getCapturePoints().keySet().stream().sorted().collect(Collectors.toList());

            if(!capturePoints.isEmpty()) {

                lore.add(Component.text("Capture Points:")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));

                for(String capturePoint : capturePoints) {
                            lore.add(Component.text(" - ")
                                    .color(NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false)
                                    .append(Component.text(TextUtil.camelCasing(capturePoint))
                                            .color(NamedTextColor.WHITE)
                                            .decoration(TextDecoration.ITALIC, false)));
                }

                lore.add(Component.empty());
            }
        }

        lore.add(ComponentUtil.leftClick(true).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("to edit")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));

        lore.add(ComponentUtil.rightClick(true).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("to load and teleport")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));

        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }


}
