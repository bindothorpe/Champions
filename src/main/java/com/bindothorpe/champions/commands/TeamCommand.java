package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class TeamCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("team")
                .executes((ctx -> TeamCommand.handleWithoutArgs(dc, ctx)))
                .then(Commands.argument("color", StringArgumentType.word())
                        .suggests(TeamCommand::getSkillSlotSuggestions)
                        .executes(ctx -> TeamCommand.handleWithColorArg(dc, ctx)));
    }

    private static int handleWithoutArgs(DomainController dc, CommandContext<CommandSourceStack> ctx) {
        if(!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can perform this command.");
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }

        TeamColor color = dc.getTeamManager().getTeamFromEntity(player);
        ChatUtil.sendMessage(player, ChatUtil.Prefix.GAME,
                Component.text("You are on the ").color(NamedTextColor.GRAY)
                        .append(Component.text(color.toString()).color(color.getTextColor()))
                        .append(Component.text(" team.").color(NamedTextColor.GRAY)));

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static int handleWithColorArg(DomainController dc, CommandContext<CommandSourceStack> ctx) {
        if(!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can perform this command.");
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }

        TeamColor teamColor;
        try {
            teamColor = TeamColor.valueOf(StringArgumentType.getString(ctx, "color"));
        } catch (IllegalArgumentException e) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Invalid team color.").color(NamedTextColor.GRAY));
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }

        dc.getTeamManager().addEntityToTeam(player, teamColor);
        ChatUtil.sendMessage(player, ChatUtil.Prefix.GAME,
                Component.text("You are now on the ").color(NamedTextColor.GRAY)
                        .append(Component.text(teamColor.toString()).color(teamColor.getTextColor()))
                        .append(Component.text(" team.").color(NamedTextColor.GRAY)));

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> getSkillSlotSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {

        Arrays.stream(TeamColor.values())
                .filter(teamColor -> teamColor.toString().toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(teamColor -> builder.suggest(teamColor.toString().toUpperCase()));


        return builder.buildFuture();
    }
}
