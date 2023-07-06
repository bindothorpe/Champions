package com.bindothorpe.champions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatUtil {

    private static final Component GAME_PREFIX = Component.text("Game> ").color(NamedTextColor.BLUE);

    public static void sendGameMessage(Player player, Component message) {
        player.sendMessage(GAME_PREFIX.append(message));
    }

    public static void sendGameBroadcast(Component message) {
        Bukkit.broadcast(GAME_PREFIX.append(message));
    }
}
