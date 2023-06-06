package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.gui.TestGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuildCommand implements CommandExecutor {

    private DomainController dc;

    public BuildCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You need to be a player to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;
        dc.openClassOverviewGui(player.getUniqueId());

        return true;
    }
}
