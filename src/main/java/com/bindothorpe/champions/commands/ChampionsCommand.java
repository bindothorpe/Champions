package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.ReloadResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ChampionsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("champions")
                .executes(ChampionsCommand::handleWithoutArgs)
                .then(Commands.literal("reload")
                        .executes(ChampionsCommand::handleReloadWithoutArgs)
                        .then(Commands.literal("skills")
                                .executes((ctx) -> ChampionsCommand.handleReloadSkills(ctx, dc))));

    }

    private static int handleWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        //TODO: Show version info about this plugin.

        return Command.SINGLE_SUCCESS;
    }

    private static int handleReloadWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        //TODO: Show reload options.

        return Command.SINGLE_SUCCESS;
    }

    private static int handleReloadSkills(CommandContext<CommandSourceStack> ctx, DomainController dc) {

        //TODO: Reload all skills using SkillConfig data.
        dc.getCustomConfigManager().reloadConfig("skill_config");
        ReloadResult result = dc.getSkillManager().reloadAllSkillData();

        if(ctx.getSource().getSender() instanceof Player player) {
            player.sendMessage("§f------------[ §eSkill reload completed §f]------------");
            for(ReloadResult.ResultState state: ReloadResult.ResultState.values()) {
                player.sendMessage(String.format("§f%s: §7%d", state.getLabel(), result.getResultCount(state)));
            }
            player.sendMessage("§f---------------------------------------------");
        }

        dc.getPlugin().getLogger().info("------------[ Skill reload completed ]------------");
        for(ReloadResult.ResultState state: ReloadResult.ResultState.values()) {
            dc.getPlugin().getLogger().info(String.format("%s: %d", state.getLabel(), result.getResultCount(state)));
        }
        dc.getPlugin().getLogger().info("--------------------------------------------------");

        return Command.SINGLE_SUCCESS;
    }
}
