package com.bindothorpe.champions.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static boolean isSword(Material material) {
        return material.toString().contains("SWORD");
    }

    public static boolean isAxe(Material material) {
        return material.toString().contains("_AXE");
    }

    public static boolean isBow(Material material) {
        return material.toString().contains("BOW");
    }
}
