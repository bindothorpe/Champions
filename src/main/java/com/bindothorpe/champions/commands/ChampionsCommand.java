package com.bindothorpe.champions.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ChampionsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("champions")
                .executes(ChampionsCommand::handleWithoutArgs)
                .then(Commands.literal("reload")
                        .executes(ChampionsCommand::handleReloadWithoutArgs)
                        .then(Commands.literal("skills")
                                .executes(ChampionsCommand::handleReloadSkills)));

    }

    private static int handleWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        //TODO: Show version info about this plugin.

        return Command.SINGLE_SUCCESS;
    }

    private static int handleReloadWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        //TODO: Show reload options.

        return Command.SINGLE_SUCCESS;
    }

    private static int handleReloadSkills(CommandContext<CommandSourceStack> ctx) {

        //TODO: Reload all skills using SkillConfig data.

        return Command.SINGLE_SUCCESS;
    }
}
