package com.bindothorpe.champions.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SoundCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 3) {
            player.sendMessage("Usage: /sound <sound> <volume> <pitch>");
            return true;
        }
        try {

            Sound sound = Sound.valueOf(args[0].toUpperCase());
            float volume = Float.parseFloat(args[1]);
            float pitch = Float.parseFloat(args[2]);

            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid sound!");
            return true;
        }


        return true;
    }
}
