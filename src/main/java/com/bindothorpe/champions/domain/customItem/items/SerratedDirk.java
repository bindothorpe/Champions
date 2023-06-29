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
import java.util.Set;

public class SerratedDirk extends CustomItem {

    public SerratedDirk(CustomItemManager manager) {
        super(manager, CustomItemId.SERRATED_DIRK, "Serrated Dirk", 500, List.of(CustomItemId.LONG_SWORD,CustomItemId.LONG_SWORD));
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
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
