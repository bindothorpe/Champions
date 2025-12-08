package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.gui.TestGui;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuildCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("build")
                .executes((ctx -> BuildCommand.handleWithoutArgs(dc, ctx)));
    }

    private static int handleWithoutArgs(DomainController dc, CommandContext<CommandSourceStack> ctx) {
        if(!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can perform this command.");
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }

        dc.getGuiManager().openClassOverviewGui(player.getUniqueId());

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
