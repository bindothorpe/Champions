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

    public static boolean isWeapon(ItemStack item) {
        if(item == null) return false;
        return isSword(item.getType()) || isAxe(item.getType()) || isBow(item.getType());
    }

    public static boolean isIron(Material material) {
        return material.toString().startsWith("IRON");
    }

    public static boolean isGolden(Material material) {
        return material.toString().startsWith("GOLDEN");
    }

    public static boolean isDiamond(Material material) {
        return material.toString().startsWith("DIAMOND");
    }
}
