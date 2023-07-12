package com.bindothorpe.champions.util;

import org.bukkit.entity.Player;

public class XpBarUtil {

    public static void setXp(Player player, int level, double percentageFilled) {
        // Set player level
        player.setLevel(level);

        // Calculate total experience required for the current level
        int xpToCurrentLevel = player.getExpToLevel();

        // Calculate the experience required to fill the bar to the desired percentage
        int xpToFill = (int) Math.ceil(xpToCurrentLevel * percentageFilled);

        // Set player experience
        player.setExp((float) xpToFill / xpToCurrentLevel);
    }
}
