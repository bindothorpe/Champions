package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CapturePointCommand implements CommandExecutor {
    private final DomainController dc;

    public CapturePointCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You need to be a player to perform this command.");
            return true;
        }

        if(strings.length != 2) {
            commandSender.sendMessage("Invalid number of arguments.");
            return true;
        }

        Player player = (Player) commandSender;
        String arg1 = strings[0];
        String arg2 = strings[1];

        if(!arg1.equals("create")) {
            player.sendMessage("Invalid argument " + arg1);
            return true;
        }

        if(!player.getLocation().clone().subtract(0, 2, 0).getBlock().getType().equals(Material.BEACON)) {
            player.sendMessage("You need to be standing on a beacon to create a capture point.");
            return true;
        }

        boolean success = dc.getGameManager().addCapturePoint(new CapturePoint(dc.getGameManager(), arg2, player.getLocation().toVector(), player.getWorld()));
        if(success) {
            player.sendMessage("Capture point created.");
        } else {
            player.sendMessage("Capture point already exists.");
        }
        return true;
    }
}
