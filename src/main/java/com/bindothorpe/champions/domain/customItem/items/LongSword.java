package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LongSword extends CustomItem {


    public LongSword(CustomItemManager manager) {
        super(manager, CustomItemId.LONG_SWORD, "Long Sword", 500);
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(getName()));
        meta.lore(getLore());

        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected List<Component> getLore() {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(" "));
        lore.add(Component.text("§7Tier: §f" + getTier()));
        lore.add(Component.text(" "));

        return lore;
    }
}
