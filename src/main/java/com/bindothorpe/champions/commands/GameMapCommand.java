package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMapData;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameMapCommand {

    private static final Map<UUID, GameMapData> editingPlayers = new HashMap<>();

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("map")
                .executes(ctx -> handleNoArgs(ctx, dc))
                .then(Commands.literal("create")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleCreate(ctx, dc))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleDelete(ctx, dc))))
                .then(Commands.literal("edit")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleEdit(ctx, dc))))
                .then(Commands.literal("tp")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleTeleport(ctx, dc))))
                .then(Commands.literal("tp-all")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleTpAll(ctx, dc))))
                .then(Commands.literal("add-cp")
                        .then(Commands.argument("capturePointName", StringArgumentType.word())
                                .executes(ctx -> handleAddCapturePoint(ctx, dc))))
                .then(Commands.literal("add-sp")
                        .then(Commands.argument("teamColor", StringArgumentType.word())
                                .executes(ctx -> handleAddSpawnPoint(ctx, dc))))
                .then(Commands.literal("info")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleInfo(ctx, dc))))
                .then(Commands.literal("load")
                        .then(Commands.argument("mapName", StringArgumentType.word())
                                .executes(ctx -> handleLoad(ctx, dc))))
                .then(Commands.literal("unload")
                        .executes(ctx -> handleUnload(ctx, dc)));
    }

    private static int handleNoArgs(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        dc.getGuiManager().openMapMainGui(player.getUniqueId());
        return Command.SINGLE_SUCCESS;
    }

    private static int handleCreate(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        String mapName = StringArgumentType.getString(ctx, "mapName");

        if (dc.getGameMapManager().createGameMap(mapName)) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Created map: " + mapName).color(NamedTextColor.GRAY));
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Map already exists: " + mapName).color(NamedTextColor.GRAY));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int handleDelete(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Deleting map.").color(NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int handleEdit(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        String mapName = StringArgumentType.getString(ctx, "mapName");
        UUID playerId = player.getUniqueId();

        if (editingPlayers.containsKey(playerId)) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Stopped editing map: " + mapName).color(NamedTextColor.GRAY));
            editingPlayers.remove(playerId);
            return Command.SINGLE_SUCCESS;
        }

        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);
        if (mapData != null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Editing map: " + mapName).color(NamedTextColor.GRAY));
            editingPlayers.put(playerId, mapData);
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Map does not exist: " + mapName).color(NamedTextColor.GRAY));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int handleTeleport(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        String mapName = StringArgumentType.getString(ctx, "mapName");
        dc.getGameMapManager().teleportToMap(player, mapName);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleTpAll(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        String mapName = StringArgumentType.getString(ctx, "mapName");
        dc.getGameMapManager().teleportAllToMap(new ArrayList<>(Bukkit.getOnlinePlayers()), mapName);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleAddCapturePoint(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        GameMapData gameMapData = editingPlayers.get(player.getUniqueId());
        if (gameMapData == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("No map selected. Use /map edit <name> first.").color(NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        String capturePointName = StringArgumentType.getString(ctx, "capturePointName");
        Vector v = player.getLocation().toVector();
        gameMapData.addCapturePoint(capturePointName, new Vector(v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5));
        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Added capture point: " + capturePointName).color(NamedTextColor.GRAY));

        return Command.SINGLE_SUCCESS;
    }

    private static int handleAddSpawnPoint(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        GameMapData gameMapData = editingPlayers.get(player.getUniqueId());
        if (gameMapData == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("No map selected. Use /map edit <name> first.").color(NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        String teamColorStr = StringArgumentType.getString(ctx, "teamColor");
        TeamColor team;

        try {
            team = TeamColor.valueOf(teamColorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Invalid team: " + teamColorStr).color(NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        Vector v = player.getLocation().toVector();
        gameMapData.addSpawnPoint(team, new Vector(v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5), player.getLocation().getDirection());
        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Added spawn point for team: " + team.name()).color(NamedTextColor.GRAY));

        return Command.SINGLE_SUCCESS;
    }

    private static int handleInfo(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        String mapName = StringArgumentType.getString(ctx, "mapName");
        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);

        if (mapData == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Map does not exist: " + mapName).color(NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        NamedTextColor lineColor = NamedTextColor.YELLOW;
        NamedTextColor crystalColor = NamedTextColor.WHITE;
        Component line = Component.text("⎯⎯⎯⎯⎯⎯⎯").color(lineColor).decoration(TextDecoration.STRIKETHROUGH, true).decoration(TextDecoration.BOLD, false)
                .append(Component.text("≺").color(lineColor).decoration(TextDecoration.STRIKETHROUGH, false).decoration(TextDecoration.BOLD, true))
                .append(Component.text("♦").color(crystalColor).decoration(TextDecoration.STRIKETHROUGH, false).decoration(TextDecoration.BOLD, false))
                .append(Component.text("≻").color(lineColor).decoration(TextDecoration.STRIKETHROUGH, false).decoration(TextDecoration.BOLD, true))
                .append(Component.text("⎯⎯⎯⎯⎯⎯⎯").color(lineColor).decoration(TextDecoration.STRIKETHROUGH, true).decoration(TextDecoration.BOLD, false));

        Component fullLine = Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯").color(lineColor).decoration(TextDecoration.STRIKETHROUGH, true).decoration(TextDecoration.BOLD, false);

        player.sendMessage(line);
        player.sendMessage(Component.text("Map: ").color(NamedTextColor.YELLOW).append(Component.text(mapName).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.empty());
        player.sendMessage(fullLine);
        player.sendMessage(Component.text("Capture Points: ").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.empty());

        if (mapData.getCapturePoints().isEmpty()) {
            player.sendMessage(Component.text("None").color(NamedTextColor.WHITE));
        } else {
            mapData.getCapturePoints().forEach((name, location) -> {
                player.sendMessage(Component.text("  " + name + ": ").color(NamedTextColor.YELLOW).append(Component.text(location.toString()).color(NamedTextColor.WHITE)));
            });
        }

        player.sendMessage(Component.empty());
        player.sendMessage(fullLine);
        player.sendMessage(Component.text("Spawn Points: ").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.empty());

        if (mapData.getSpawnPoints().isEmpty()) {
            player.sendMessage(Component.text("None").color(NamedTextColor.WHITE));
            player.sendMessage(Component.empty());
        } else {
            mapData.getSpawnPoints().forEach((team, locations) -> {
                player.sendMessage(Component.text("  " + TextUtil.camelCasing(team.name()) + ": ").color(team.getTextColor()));
                locations.forEach(location -> {
                    player.sendMessage(Component.text("    " + location.toString()).color(NamedTextColor.WHITE));
                });
            });
            player.sendMessage(Component.empty());
        }

        player.sendMessage(line);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleLoad(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        String mapName = StringArgumentType.getString(ctx, "mapName");
        boolean success = dc.getGameMapManager().loadMap(mapName);

        if (!success) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Failed to load map.").color(NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Loaded map: " + mapName).color(NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int handleUnload(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can use this command");
            return Command.SINGLE_SUCCESS;
        }

        World world = Bukkit.getWorld("world");

        if (world == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Failed to unload map.").color(NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        dc.getGameMapManager().unloadMap();
        return Command.SINGLE_SUCCESS;
    }
}