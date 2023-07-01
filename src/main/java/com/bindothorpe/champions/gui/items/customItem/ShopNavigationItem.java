package com.bindothorpe.champions.gui.items.customItem;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ShopNavigationItem extends GuiItem {
    private final DomainController dc;
    public ShopNavigationItem(@NotNull CustomItemType type, DomainController dc) {
        super(new ItemStack(type.getMaterial()));
        this.dc = dc;
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text(
                        TextUtil.camelCasing(type.name())
                ).color(
                        type.getColor()
                ));

        meta.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        setItem(item);

        setAction(event -> {
            dc.openShopHomeGui(event.getWhoClicked().getUniqueId(), type);
        });
    }
}
