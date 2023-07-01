package com.bindothorpe.champions.domain.customItem;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum CustomItemType {
    ATTACK(NamedTextColor.RED, Material.IRON_SWORD),
    DEFENSE(NamedTextColor.BLUE, Material.DIAMOND_CHESTPLATE),
    UTILITY(NamedTextColor.GREEN, Material.BOOK);

    final NamedTextColor color;
    final Material material;

    CustomItemType(NamedTextColor color, Material material) {
        this.color = color;
        this.material = material;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Material getMaterial() {
        return material;
    }
}
