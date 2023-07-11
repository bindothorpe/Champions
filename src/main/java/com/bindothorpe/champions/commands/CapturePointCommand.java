package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            ChatUtil.sendMessage((Player) commandSender, ChatUtil.Prefix.ERROR, Component.text("Invalid arguments.").color(NamedTextColor.GRAY));
            return true;
        }

        Player player = (Player) commandSender;
        String arg1 = strings[0];
        String arg2 = strings[1];

        if(!arg1.equals("create")) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Invalid argument.").color(NamedTextColor.GRAY));
            return true;
        }

        if(!player.getLocation().clone().subtract(0, 2, 0).getBlock().getType().equals(Material.BEACON)) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("You must be standing on a beacon to create a capture point.").color(NamedTextColor.GRAY));
            return true;
        }

        boolean success = dc.getGameManager().addCapturePoint(new CapturePoint(dc.getGameManager(), arg2, player.getLocation().toVector(), player.getWorld()));
        if(success) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Capture point created.").color(NamedTextColor.GRAY));
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.ERROR, Component.text("Capture already exists.").color(NamedTextColor.GRAY));
        }
        return true;
    }
}
