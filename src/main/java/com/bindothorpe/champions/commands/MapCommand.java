package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.LocalGameMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MapCommand implements CommandExecutor {

    private final DomainController dc;
    private final Map<String, LocalGameMap> maps = new HashMap<>();

    public MapCommand(DomainController dc) {
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
            player.sendMessage("Usage: /map <create|delete|list|tp> <mapName>");
            return true;
        }

        String action = args[0];
        String mapName = args[1];

        if(action.equalsIgnoreCase("create")) {
            createMap(player, mapName);
        } else if(action.equalsIgnoreCase("delete")) {
            player.sendMessage("Deleting map: " + mapName);
        } else if(action.equalsIgnoreCase("list")) {
            player.sendMessage("Listing maps");
        } else if(action.equalsIgnoreCase("tp")) {
            tpToMap(player, mapName);
        } else {
            player.sendMessage("Unknown action: " + action);
        }


        return true;
    }
    private void createMap(Player player, String mapName) {
        if(maps.containsKey(mapName)) {
            player.sendMessage("Map already exists: " + mapName);
            return;
        }

        maps.put(mapName, new LocalGameMap(dc.getGameMapManager().getGameMapsFolder(), mapName, true));
    }

    private void tpToMap(Player player, String mapName) {
        if(!maps.containsKey(mapName)) {
            player.sendMessage("Map does not exist: " + mapName);
            return;
        }

        player.teleport(maps.get(mapName).getWorld().getSpawnLocation());
    }

}
