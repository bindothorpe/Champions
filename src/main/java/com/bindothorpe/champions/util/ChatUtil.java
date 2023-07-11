package com.bindothorpe.champions.util;

import com.bindothorpe.champions.DomainController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class ChatUtil {
    public static void sendMessage(Player player, Prefix prefix, Component message) {
        player.sendMessage(prefix.component().append(message));
    }

    public static void sendGameBroadcast(Prefix prefix, Component message) {
        Bukkit.broadcast(prefix.component().append(message));
    }


    public static void sendCountdown(DomainController dc, Collection<Player> players, int seconds, String message, Runnable onFinish) {
        sendCountdown(dc, players, seconds, Prefix.GAME, message, NamedTextColor.GRAY, NamedTextColor.YELLOW, NamedTextColor.GOLD, 3, onFinish);

    }

    public static void sendCountdown(DomainController dc, Collection<Player> players, int seconds, Prefix prefix, String message, NamedTextColor textColor, NamedTextColor highlightColor, NamedTextColor highlightColor2, int highlightTwo, Runnable onFinish) {
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
                    sendMessage(player, prefix, formattedMessage);
                }
                countdownTime--;
            }
        }.runTaskTimer(dc.getPlugin(), 0L, 20L);
    }

    public enum Prefix {
        GAME(Component.text("Game> ").color(NamedTextColor.BLUE)),
        SKILL(Component.text("Skill> ").color(NamedTextColor.BLUE)),
        COOLDOWN(Component.text("Cooldown> ").color(NamedTextColor.BLUE)),
        ERROR(Component.text("Error> ").color(NamedTextColor.RED)),
        PLUGIN(Component.text("Champions> ").color(NamedTextColor.GOLD)),
        DEBUG(Component.text("Debug> ").color(NamedTextColor.GREEN)),
        MAP(Component.text("Map> ").color(NamedTextColor.LIGHT_PURPLE));

        private final Component prefix;

        Prefix(Component prefix) {
            this.prefix = prefix;
        }

        public Component component() {
            return prefix;
        }
    }
}
