package com.bindothorpe.champions.util;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.team.TeamColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.Prefix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public class ChatUtil {
    public static void sendMessage(Player player, Prefix prefix, Component message) {
        if(prefix == null) {
            player.sendMessage(message);
        } else {
            player.sendMessage(prefix.component().append(message));
        }
    }

    public static void sendMessage(Player player, Component prefix, Component message) {
        if(prefix == null) {
            player.sendMessage(message);
        } else {
            player.sendMessage(prefix.append(message));
        }
    }

    public static void sendMessage(Player player, Component message) {
        sendMessage(player, (Component) null, message);
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

    /**
     * Sends a skill hit message to the caster and/or receiver of a skill.
     * <p>
     * This method displays formatted messages indicating that a skill has hit an entity.
     * The messages include the skill name, and are color-coded based on team affiliation.
     * </p>
     *
     * @param dc             the domain controller used to access team management
     * @param caster         the player who cast the skill
     * @param receiver       the entity that was hit by the skill
     * @param skillName      the name of the skill that was used
     * @param sendToReceiver whether to send a message to the receiver (only if receiver is a Player)
     * @param sendToCaster   whether to send a message to the caster
     * @throws NullPointerException if dc, caster, receiver, or skillName is null
     */
    public static void sendSkillHitMessage(@NotNull DomainController dc, @NotNull Player caster, @NotNull Entity receiver, @NotNull String skillName, boolean sendToReceiver, boolean sendToCaster) {

        if(sendToCaster) {
            TeamColor teamColor = dc.getTeamManager().getTeamFromEntity(receiver);
            NamedTextColor textColor = teamColor == null ? NamedTextColor.WHITE : teamColor.getTextColor();
            sendMessage(caster, Prefix.SKILL, Component.text("You hit ").color(NamedTextColor.GRAY)
                    .append(Component.text(receiver.getName(), textColor))
                    .append(Component.text(" with ", NamedTextColor.GRAY))
                    .append(Component.text(skillName).color(NamedTextColor.YELLOW))
                    .append(Component.text(".").color(NamedTextColor.GRAY)));
        }

        if(sendToReceiver && receiver instanceof Player receiverPlayer) {
            TeamColor teamColor = dc.getTeamManager().getTeamFromEntity(caster);
            NamedTextColor textColor = teamColor == null ? NamedTextColor.WHITE : teamColor.getTextColor();
            sendMessage(
                    receiverPlayer,
                    Prefix.SKILL,
                    Component.text(caster.getName(), textColor)
                            .append(Component.text(" hit you with ", NamedTextColor.GRAY))
                            .append(Component.text(skillName, NamedTextColor.YELLOW))
                            .append(Component.text(" level ", NamedTextColor.GRAY))
                            .append(Component.text(".", NamedTextColor.GRAY))
            );
        }
    }

    /**
     * Sends a skill hit message to both the caster and receiver.
     * <p>
     * This is a convenience method that sends messages to both parties involved in the skill hit.
     * </p>
     *
     * @param dc        the domain controller used to access team management
     * @param caster    the player who cast the skill
     * @param receiver  the entity that was hit by the skill
     * @param skillName the name of the skill that was used
     * @see #sendSkillHitMessage(DomainController, Player, Entity, String, boolean, boolean)
     */
    public static void sendSkillHitMessage(@NotNull DomainController dc, @NotNull Player caster, @NotNull Entity receiver, @NotNull String skillName) {
        sendSkillHitMessage(dc, caster, receiver, skillName, true, true);
    }

    /**
     * Sends a skill hit message only to the caster.
     * <p>
     * Useful when you only want to notify the attacker that their skill landed successfully.
     * </p>
     *
     * @param dc        the domain controller used to access team management
     * @param caster    the player who cast the skill
     * @param receiver  the entity that was hit by the skill
     * @param skillName the name of the skill that was used
     * @see #sendSkillHitMessage(DomainController, Player, Entity, String, boolean, boolean)
     */
    public static void sendSkillHitMessageToCaster(@NotNull DomainController dc, @NotNull Player caster, @NotNull Entity receiver, @NotNull String skillName) {
        sendSkillHitMessage(dc, caster, receiver, skillName, false, true);
    }

    /**
     * Sends a skill hit message only to the receiver.
     * <p>
     * Useful when you only want to notify the target that they were hit by a skill.
     * The receiver must be a Player to receive the message.
     * </p>
     *
     * @param dc        the domain controller used to access team management
     * @param caster    the player who cast the skill
     * @param receiver  the entity that was hit by the skill (must be a Player to receive message)
     * @param skillName the name of the skill that was used
     * @see #sendSkillHitMessage(DomainController, Player, Entity, String, boolean, boolean)
     */
    public static void sendSkillHitMessageToReceiver(@NotNull DomainController dc, @NotNull Player caster, @NotNull Entity receiver, @NotNull String skillName) {
        sendSkillHitMessage(dc, caster, receiver, skillName, true, false);
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
