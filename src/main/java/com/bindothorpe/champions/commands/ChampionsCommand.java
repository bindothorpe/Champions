package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.commands.champions.MapCommand;
import com.bindothorpe.champions.domain.skill.ReloadResult;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChampionsCommand {

    private static final Map<String, World> worldMap = new HashMap<>();


    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("champions")
                .executes(ChampionsCommand::handleWithoutArgs)
                .then(Commands.literal("reload")
                        .executes(ChampionsCommand::handleReloadWithoutArgs)
                        .then(Commands.literal("skills")
                                .executes((ctx) -> ChampionsCommand.handleReloadSkills(ctx, dc))))
                .then(MapCommand.createMapCommand(dc))
                .then(Commands.literal("world")
                        .then(Commands.literal("load")
                                .executes((ctx) -> ChampionsCommand.handleWorldLoadWithoutArgs(ctx, dc))
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("alias", StringArgumentType.word())
                                                .executes((ctx) -> ChampionsCommand.handleWorldLoadWithArgs(ctx, dc)))))
                        .then(Commands.literal("import")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes((ctx) -> ChampionsCommand.handleWorldImportWithArgs(ctx, dc))))
                        .then(Commands.literal("unload")
                                .then(Commands.argument("alias", StringArgumentType.word())
                                        .executes((ctx) -> ChampionsCommand.handleWorldUnloadWithArgs(ctx, dc))))
                        .then(Commands.literal("tp")
                                .executes(ChampionsCommand::handleWorldTpWithoutArgs)
                                .then(Commands.argument("alias", StringArgumentType.word())
                                        .executes(ChampionsCommand::handleWorldTpWithArgs))));
    }

    private static int handleWorldUnloadWithArgs(CommandContext<CommandSourceStack> ctx, DomainController dc) {

        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String worldAlias = StringArgumentType.getString(ctx, "alias");
        if(!worldMap.containsKey(worldAlias)) {
            ChatUtil.sendMessage(player,
                    ChatUtil.Prefix.MAP,
                    Component.text(String.format("World with alias '%s' does not exist exists.", worldAlias), NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        for(Player playerInWorld: worldMap.get(worldAlias).getPlayers()) {
            playerInWorld.teleport(dc.getPlugin().getServer().getRespawnWorld().getSpawnLocation());
        }

        Bukkit.unloadWorld(worldMap.remove(worldAlias), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleWorldImportWithArgs(CommandContext<CommandSourceStack> ctx, DomainController dc) {

        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String worldName = StringArgumentType.getString(ctx, "name");
        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();

        File gameMapsDir = new File(dc.getPlugin().getDataFolder().getPath() + "/gameMaps");
        File worldDir = new File(gameMapsDir, worldName);
        try {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Trying to import %s.", worldName), NamedTextColor.GRAY));
            SlimeWorld world = asp.readVanillaWorld(
                    worldDir,      // Pass the world directory directly
                    worldName,
                    dc.getDatabaseController().getMysqlLoader()
            );

            asp.saveWorld(world);
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Successfully imported %s.", worldName), NamedTextColor.GRAY));
        } catch (Exception e) {
            if(e instanceof WorldAlreadyExistsException) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Failed to import %s. This world already exists.", worldName), NamedTextColor.GRAY));
                return Command.SINGLE_SUCCESS;
            }
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Failed to import %s.", worldName), NamedTextColor.GRAY));
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        //TODO: Show version info about this plugin.

        return Command.SINGLE_SUCCESS;
    }

    private static int handleReloadWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        //TODO: Show reload options.

        return Command.SINGLE_SUCCESS;
    }

    private static int handleWorldLoadWithArgs(CommandContext<CommandSourceStack> ctx, DomainController dc) {

        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String worldName = StringArgumentType.getString(ctx, "name");
        String worldAlias = StringArgumentType.getString(ctx, "alias");

        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();

        if(worldMap.containsKey(worldAlias)) {
            ChatUtil.sendMessage(player,
                    ChatUtil.Prefix.MAP,
                    Component.text(String.format("World with alias '%s' already exists.", worldName), NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        try {
            if(!dc.getDatabaseController().getMysqlLoader().worldExists(worldName)) {
                ChatUtil.sendMessage(player,
                        ChatUtil.Prefix.MAP,
                        Component.text(String.format("World '%s' does not exist.", worldAlias), NamedTextColor.GRAY));
                return Command.SINGLE_SUCCESS;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            SlimeWorld slimeWorld = asp.readWorld(dc.getDatabaseController().getMysqlLoader(), worldName, false, new SlimePropertyMap());
            SlimeWorldInstance slimeWorldInstance = asp.loadWorld(slimeWorld, true);
            worldMap.put(worldAlias, slimeWorldInstance.getBukkitWorld());
            ChatUtil.sendMessage(player,
                    ChatUtil.Prefix.MAP,
                    Component.text(String.format("Successfully loaded '%s' with alias '%s'.", worldName, worldAlias), NamedTextColor.GRAY));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleWorldLoadWithoutArgs(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        try {
            ChatUtil.sendMessage(player,
                    ChatUtil.Prefix.MAP,
                    Component.text("Available worlds:", NamedTextColor.GRAY));
            for(String worldName : dc.getDatabaseController().getMysqlLoader().listWorlds()) {
                ChatUtil.sendMessage(player,
                        Component.text("- ", NamedTextColor.GRAY),
                        Component.text(worldName, NamedTextColor.WHITE));
            }
        } catch (IOException e) {
            dc.getPlugin().getLogger().warning("Not directory exception: " + e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }


    private static int handleWorldTpWithoutArgs(CommandContext<CommandSourceStack> ctx) {

        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        ChatUtil.sendMessage(player,
                ChatUtil.Prefix.MAP,
                Component.text("Available worlds:", NamedTextColor.GRAY));

        for(String worldAlias: worldMap.keySet()) {
            ChatUtil.sendMessage(player,
                    Component.text("- ", NamedTextColor.GRAY),
                    Component.text(worldAlias, NamedTextColor.WHITE).append(Component.text(String.format(" (%s)", worldMap.get(worldAlias).getName()), NamedTextColor.GRAY)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleWorldTpWithArgs(CommandContext<CommandSourceStack> ctx) {

        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String worldAlias = StringArgumentType.getString(ctx, "alias");
        if(!worldMap.containsKey(worldAlias)) {
            ChatUtil.sendMessage(player,
                    ChatUtil.Prefix.MAP,
                    Component.text(String.format("World with alias '%s' does not exist exists.", worldAlias), NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        player.teleport(worldMap.get(worldAlias).getSpawnLocation());

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
