package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {

    private final DomainController dc;

    public ShopCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;

        if(strings.length == 0) {
            dc.openShopGui(player.getUniqueId(), null);
            return true;
        }

        try {
            dc.openShopGui(player.getUniqueId(), CustomItemId.valueOf(strings[0]));
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid custom item id");
        }

        return true;
    }
}
