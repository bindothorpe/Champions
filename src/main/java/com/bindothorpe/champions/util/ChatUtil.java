package com.bindothorpe.champions.util;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.Skill;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.function.Function;

public class ChatUtil {
    public static void sendMessage(Player player, Prefix prefix, Component message) {
        player.sendMessage(prefix.component().append(message));
    }

    public static void sendActionBarMessage(Player player, Component message) {
        player.sendActionBar(message);
    }
    public static void sendBroadcast(Prefix prefix, Component message) {
        Bukkit.broadcast(prefix.component().append(message));
    }

    public static void sendSkillMessage(Player player, String skillName, int level) {
        sendMessage(player, Prefix.SKILL, Component.text("You used ").color(NamedTextColor.GRAY)
                .append(Component.text(skillName).color(NamedTextColor.YELLOW))
                .append(Component.text(" level ").color(NamedTextColor.GRAY))
                .append(Component.text(level).color(NamedTextColor.YELLOW))
                .append(Component.text(".").color(NamedTextColor.GRAY)));
    }


    public static void sendCountdown(DomainController dc, Collection<Player> players, int seconds, String message, Runnable onFinish, Function<Integer, Void> onTick) {
        sendCountdown(dc, players, seconds, Prefix.GAME, message, NamedTextColor.GRAY, NamedTextColor.YELLOW, NamedTextColor.GOLD, 3, onFinish, onTick);

    }

    public static void sendCountdown(DomainController dc, Collection<Player> players, int seconds, Prefix prefix, String message, NamedTextColor textColor, NamedTextColor highlightColor, NamedTextColor highlightColor2, int highlightTwo, Runnable onFinish, Function<Integer, Void> onTick) {
        new BukkitRunnable() {
            int countdownTime = seconds;

            @Override
            public void run() {

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
                onTick.apply(countdownTime);

                if (countdownTime <= 1) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onFinish.run();
                        }
                    }.runTask(dc.getPlugin());
                    this.cancel();
                    return;
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
        MAP(Component.text("Map> ").color(NamedTextColor.LIGHT_PURPLE)),
        EFFECT(Component.text("Effect> ").color(NamedTextColor.BLUE));

        private final Component prefix;

        Prefix(Component prefix) {
            this.prefix = prefix;
        }

        public Component component() {
            return prefix;
        }
    }
}
