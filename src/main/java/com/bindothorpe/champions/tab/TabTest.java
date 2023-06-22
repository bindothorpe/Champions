package com.bindothorpe.champions.tab;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TabTest {

    private static TabAPI tabAPI = TabAPI.getInstance();
    private static ScoreboardManager scoreboardManager = tabAPI.getScoreboardManager();

    public static void display(Player player) {
        scoreboardManager.createScoreboard("side", "Info", Arrays.asList("Line 1", "Line 2", "Line 3"));
//        scoreboardManager.showScoreboard(tabAPI.getPlayer(player.getUniqueId()), "side");
    }
}
