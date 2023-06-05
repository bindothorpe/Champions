package com.bindothorpe.champions.gui.items;

import com.bindothorpe.champions.domain.ClassType;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ClassIconItem extends GuiItem {
    public ClassIconItem(ClassType classType) {
        super(new ItemStack(Material.LEATHER_HELMET));
        ItemStack item;
        ItemMeta meta;
        switch (classType) {
            case BRUTE:
                item = new ItemStack(Material.DIAMOND_HELMET);
                meta = item.getItemMeta();
                meta.setDisplayName("Brute");
                item.setItemMeta(meta);
                setItem(item);
                break;


            case RANGER:
                item = new ItemStack(Material.CHAINMAIL_HELMET);
                meta = item.getItemMeta();
                meta.setDisplayName("Ranger");
                item.setItemMeta(meta);
                setItem(item);
                break;
            case KNIGHT:
                item = new ItemStack(Material.IRON_HELMET);
                meta = item.getItemMeta();
                meta.setDisplayName("Knight");
                item.setItemMeta(meta);
                setItem(item);
                break;
            case MAGE:
                item = new ItemStack(Material.GOLDEN_HELMET);
                meta = item.getItemMeta();
                meta.setDisplayName("Mage");
                item.setItemMeta(meta);
                setItem(item);
                break;
            default:
                item = new ItemStack(Material.LEATHER_HELMET);
                meta = item.getItemMeta();
                meta.setDisplayName("Assassin");
                item.setItemMeta(meta);
                setItem(item);
                break;
        }

        setAction(this::handleClick);
    }


    private void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if(event.getClick().equals(ClickType.LEFT)) {
            event.getWhoClicked().sendMessage(Component.text("You left clicked"));
        } else if (event.getClick().equals(ClickType.RIGHT)) {
            event.getWhoClicked().sendMessage(Component.text("You right clicked (you created a new build)"));
        }
    }
}
