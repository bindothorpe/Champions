package com.bindothorpe.champions.util;

import com.bindothorpe.champions.DomainController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class ChatUtil {

    private static final Component GAME_PREFIX = Component.text("Game> ").color(NamedTextColor.BLUE);

    public static void sendGameMessage(Player player, Component message) {
        player.sendMessage(GAME_PREFIX.append(message));
    }

    public static void sendGameBroadcast(Component message) {
        Bukkit.broadcast(GAME_PREFIX.append(message));
    }


    public static void sendCountdown(DomainController dc, Collection<Player> players, int seconds, String message, Runnable onFinish) {
        sendCountdown(dc, players, seconds, message, NamedTextColor.GRAY, NamedTextColor.YELLOW, NamedTextColor.GOLD, 3, onFinish);

    }

    public static void sendCountdown(DomainController dc, Collection<Player> players, int seconds, String message, NamedTextColor textColor, NamedTextColor highlightColor, NamedTextColor highlightColor2, int highlightTwo, Runnable onFinish) {
        new BukkitRunnable() {
            int countdownTime = seconds;

            @Override
            public void run() {
                if (countdownTime < 1) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onFinish.run();
                        }
                    }.runTask(dc.getPlugin());
                    this.cancel();
                }

                String[] splitMessage = message.split("%s");
                Component formattedMessage;
                if (splitMessage.length == 1) {
                    formattedMessage= Component.text(message).color(textColor);
                } else {
                    formattedMessage = Component.empty();

                    for (int i = 0; i < splitMessage.length; i++) {
                        String s = splitMessage[i];

                        if (i == splitMessage.length - 1) {
                            formattedMessage = formattedMessage
                                    .append(Component.text(s, textColor));

                        } else {
                            formattedMessage = formattedMessage
                                    .append(Component.text(s, textColor))
                                    .append(Component.text(countdownTime, countdownTime <= highlightTwo ? highlightColor2 : highlightColor));

                        }
                    }
                }

                for (Player player : players) {
                    sendGameMessage(player, formattedMessage);
                }
                countdownTime--;
            }
        }.runTaskTimer(dc.getPlugin(), 0L, 20L);
    }
}
