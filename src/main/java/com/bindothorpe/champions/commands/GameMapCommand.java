package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMapData;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.TextUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameMapCommand implements CommandExecutor {

    private static final List<String> ACTIONS = new ArrayList<>(List.of("create", "delete", "edit", "tp", "tp-all", "add-cp"));

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(DomainController dc) {
        return Commands.literal("map")
                .executes((ctx -> GameMapCommand.handleWithoutArgs(dc, ctx)))
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests(GameMapCommand::getActionSuggestions)
                        .then(Commands.argument("map_name", StringArgumentType.word())));
    }

    private static int handleWithoutArgs(DomainController dc, CommandContext<CommandSourceStack> ctx) {
        if(!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage("Only players can perform this command.");
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }

        dc.getGuiManager().openMapMainGui(player.getUniqueId());

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

    private static CompletableFuture<Suggestions> getActionSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {

        ACTIONS.stream()
                .filter(action -> action.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(action -> builder.suggest(action.toLowerCase()));


        return builder.buildFuture();
    }













    private final DomainController dc;
    private GameMapData gameMapData;

    public GameMapCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command");
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length < 2) {
            dc.getGuiManager().openMapMainGui(player.getUniqueId());
//            player.sendMessage("Usage: /map <create|edit|tp|load|unload|add-cp|add-sp|info> <name>");
            return true;
        }

        String action = strings[0];
        String mapName = strings[1];

        if (action.equalsIgnoreCase("create")) {
            handleCreate(player, mapName);

        } else if (action.equalsIgnoreCase("delete")) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Deleting map.").color(NamedTextColor.GRAY));

        } else if (action.equalsIgnoreCase("edit")) {
            handleEdit(player, mapName);

        } else if (action.equalsIgnoreCase("tp")) {
            handleTeleport(player, mapName);

        } else if (action.equalsIgnoreCase("tp-all")) {
            handleTpAll(mapName);

        } else if (action.equalsIgnoreCase("add-cp")) {
            if (gameMapData == null) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("No map selected.").color(NamedTextColor.GRAY));
                return true;
            }

            handleAddCapturePoint(player, strings[1]);

        } else if (action.equalsIgnoreCase("add-sp")) {
            if (gameMapData == null) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("No map selected.").color(NamedTextColor.GRAY));
                return true;
            }

            TeamColor team = null;

            try {
                team = TeamColor.valueOf(strings[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Invalid team: " + strings[1]).color(NamedTextColor.GRAY));
                return true;
            }

            handleAddSpawnPoint(player, team);

        } else if (action.equalsIgnoreCase("info")) {
            handleInfo(player, mapName);
        } else if (action.equalsIgnoreCase("load")) {
            handleLoad(player, mapName);
        } else if (action.equalsIgnoreCase("unload")) {
            handleUnload(player);
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Unknown action.").color(NamedTextColor.GRAY));
        }


        return true;
    }

    private void handleLoad(Player player, String mapName) {
        boolean success = dc.getGameMapManager().loadMap(mapName);
        if (!success) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Failed to load map.").color(NamedTextColor.GRAY));
            return;
        }

        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Loaded map: " + mapName).color(NamedTextColor.GRAY));
    }

    private void handleUnload(Player player) {
        World world = Bukkit.getWorld("world");

        if (world == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Failed to unload map.").color(NamedTextColor.GRAY));
            return;
        }

        dc.getGameMapManager().unloadMap();
    }

    private void handleTpAll(String mapName) {
        dc.getGameMapManager().teleportAllToMap(new ArrayList<>(Bukkit.getOnlinePlayers()), mapName);
    }

    private void handleTeleport(Player player, String mapName) {
        dc.getGameMapManager().teleportToMap(player, mapName);
    }

    private void handleAddSpawnPoint(Player player, TeamColor team) {
        Vector v = player.getLocation().toVector();
        gameMapData.addSpawnPoint(team, new Vector(v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5), player.getLocation().getDirection());
        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Added spawn point for team: " + team.name()).color(NamedTextColor.GRAY));
    }

    private void handleAddCapturePoint(Player player, String capturePointName) {
        Vector v = player.getLocation().toVector();
        gameMapData.addCapturePoint(capturePointName, new Vector(v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5));
        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Added spawn capture point.").color(NamedTextColor.GRAY));

    }

    private void handleCreate(Player player, String mapName) {
        if (dc.getGameMapManager().createGameMap(mapName)) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Created map: " + mapName).color(NamedTextColor.GRAY));
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Map already exists: " + mapName).color(NamedTextColor.GRAY));
        }
    }

    private void handleEdit(Player player, String mapName) {
        if (gameMapData != null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Stopped editing map: " + mapName).color(NamedTextColor.GRAY));
            this.gameMapData = null;
            return;
        }

        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);
        if (mapData != null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Editing map: " + mapName).color(NamedTextColor.GRAY));
            this.gameMapData = mapData;
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Map does not exist: " + mapName).color(NamedTextColor.GRAY));
        }
    }

    private void handleInfo(Player player, String mapName) {
        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);
        if (mapData == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Map does not exist: " + mapName).color(NamedTextColor.GRAY));
            return;
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
    }

}
