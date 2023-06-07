package com.bindothorpe.champions.gui.items.global;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.SerializedLambda;
import java.util.function.Consumer;

public class BackItem extends GuiItem {
    public BackItem(Consumer<InventoryClickEvent> action) {
        super(new ItemStack(Material.BARRIER));
        ItemMeta meta = getItem().getItemMeta();
        meta.displayName(Component.text("Back").color(NamedTextColor.RED));
        getItem().setItemMeta(meta);
        setAction(event -> {
            event.setCancelled(true);
            action.accept(event);
        });
    }
}
