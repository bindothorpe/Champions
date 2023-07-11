package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamCommand implements CommandExecutor {

    private final DomainController dc;

    public TeamCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You need to be a player to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;

        if(strings.length == 0) {

            TeamColor color = dc.getTeamFromEntity(player);
            ChatUtil.sendMessage(player, ChatUtil.Prefix.GAME,
                    Component.text("You are on the ").color(NamedTextColor.GRAY)
                    .append(Component.text(color.toString()).color(color.getTextColor()))
                    .append(Component.text(" team.").color(NamedTextColor.GRAY)));
            return true;
        } else {

            TeamColor color;
            try {
                color = TeamColor.valueOf(strings[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Invalid team color.").color(NamedTextColor.GRAY));
                return true;
            }

            dc.addEntityToTeam(player, color);
            ChatUtil.sendMessage(player, ChatUtil.Prefix.GAME,
                    Component.text("You are now on the ").color(NamedTextColor.GRAY)
                            .append(Component.text(color.toString()).color(color.getTextColor()))
                            .append(Component.text(" team.").color(NamedTextColor.GRAY)));
        }

        return true;
    }
}
