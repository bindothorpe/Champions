package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMapData;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GameMapCommand implements CommandExecutor {

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

        String[] args = strings;

        if (args.length < 2) {
            player.sendMessage("Usage: /gamemap <create|delete|edit|tp|load|add-cp|add-sp|info> <name>");
            return true;
        }

        String action = args[0];
        String mapName = args[1];

        if (action.equalsIgnoreCase("create")) {
            handleCreate(player, mapName);

        } else if (action.equalsIgnoreCase("delete")) {
            player.sendMessage("Deleting map: " + mapName);

        } else if (action.equalsIgnoreCase("edit")) {
            handleEdit(player, mapName);

        } else if (action.equalsIgnoreCase("tp")) {
            handleTeleport(player, mapName);

        } else if (action.equalsIgnoreCase("add-cp")) {
            if (gameMapData == null) {
                player.sendMessage("No map selected");
                return true;
            }

            handleAddCapturePoint(player, args[1]);

        } else if (action.equalsIgnoreCase("add-sp")) {
            if (gameMapData == null) {
                player.sendMessage("No map selected");
                return true;
            }

            TeamColor team = null;

            try {
                team = TeamColor.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("Invalid team: " + args[2]);
                return true;
            }

            handleAddSpawnPoint(player, team);

        } else if (action.equalsIgnoreCase("info")) {
            handleInfo(player, mapName);
        } else if (action.equalsIgnoreCase("load")) {
            handleLoad(player, mapName);
        } else {
            player.sendMessage("Unknown action: " + action);
        }


        return true;
    }

    private void handleLoad(Player player, String mapName) {
        boolean success = dc.getGameMapManager().loadMap(mapName);
        if(!success) {
            player.sendMessage("Failed to load map: " + mapName);
        }

        player.sendMessage("Loaded map: " + mapName);
    }

    private void handleTeleport(Player player, String mapName) {
        dc.getGameMapManager().tpToMap(player, mapName);
    }

    private void handleAddSpawnPoint(Player player, TeamColor team) {
        Vector v = player.getLocation().toVector();
        gameMapData.addSpawnPoint(team, new Vector(v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5), player.getLocation().getDirection());
        player.sendMessage("Added spawn point for team: " + team);
    }

    private void handleAddCapturePoint(Player player, String capturePointName) {
        Vector v = player.getLocation().toVector();
        gameMapData.addCapturePoint(capturePointName, new Vector(v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5));
        player.sendMessage("Added capture point: " + capturePointName);

    }

    private void handleCreate(Player player, String mapName) {
        if (dc.getGameMapManager().createGameMap(mapName)) {
            player.sendMessage("Created map: " + mapName);
        } else {
            player.sendMessage("Map already exists: " + mapName);
        }
    }

    private void handleEdit(Player player, String mapName) {
        if(gameMapData != null) {
            player.sendMessage("Stopped editing map: " + gameMapData.getName());
            this.gameMapData = null;
            return;
        }

        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);
        if (mapData != null) {
            player.sendMessage("Editing map: " + mapName);
            this.gameMapData = mapData;
        } else {
            player.sendMessage("Map does not exist: " + mapName);
        }
    }

    private void handleInfo(Player player, String mapName) {
        GameMapData mapData = dc.getGameMapManager().getGameMapData(mapName);
        if (mapData == null) {
            player.sendMessage("Map does not exist: " + mapName);
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
