package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.GameState;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("game")
                .executes((ctx -> GameCommand.handleWithoutArgs(dc, ctx)));
    }

    private static int handleWithoutArgs(DomainController dc, CommandContext<CommandSourceStack> ctx) {
        dc.getGameManager().setNextGameState();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
