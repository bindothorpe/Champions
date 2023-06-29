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

public class DuskBlade extends CustomItem {
    public DuskBlade(CustomItemManager manager) {
        super(manager, CustomItemId.DUSK_BLADE, "Dusk Blade", 500, List.of(CustomItemId.SERRATED_DIRK, CustomItemId.PHAGE));
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
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
