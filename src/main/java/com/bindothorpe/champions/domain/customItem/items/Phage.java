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

public class Phage extends CustomItem {
    public Phage(CustomItemManager manager) {
        super(manager, CustomItemId.PHAGE, "Phage", 300, List.of(CustomItemId.LONG_SWORD));
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
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
