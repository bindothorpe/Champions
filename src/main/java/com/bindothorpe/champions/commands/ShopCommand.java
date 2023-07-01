package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
            dc.openShopHomeGui(player.getUniqueId(), CustomItemType.ATTACK);
            return true;
        }

        try {
            CustomItemId id = CustomItemId.valueOf(strings[0].toUpperCase());
            dc.openShopGui(player.getUniqueId(), id, new ArrayList<>(dc.getCustomItemManager().getCustomItem(id).getTypes()).get(0));
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid custom item id");
        }

        return true;
    }
}
