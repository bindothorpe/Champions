package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GameCommand implements CommandExecutor {

    private final DomainController dc;

    public GameCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        GameState currentState = dc.getGameState();
        dc.setNextGameState();
        GameState nextState = dc.getGameState();

        commandSender.sendMessage(Component.text("[Champions] ").color(NamedTextColor.GOLD)
                .append(Component.text(currentState.toString()).color(NamedTextColor.GRAY))
                .append(Component.text(" -> ").color(NamedTextColor.GRAY))
                .append(Component.text(nextState.toString()).color(NamedTextColor.YELLOW)));

        return true;
    }
}
