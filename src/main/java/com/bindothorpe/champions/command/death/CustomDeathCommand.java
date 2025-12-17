package com.bindothorpe.champions.command.death;

import com.bindothorpe.champions.command.Command;
import com.bindothorpe.champions.events.death.CustomDeathEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CustomDeathCommand implements Command {

    private final CustomDeathEvent customDeathEvent;

    public CustomDeathCommand(CustomDeathEvent customDeathEvent) {
        this.customDeathEvent = customDeathEvent;
    }

    @Override
    public void execute() {
        Player player = customDeathEvent.getPlayer();
        fullyHealPlayer(player);
        teleportPlayerToRespawnLocation(player);
        sendDeathMessage(player);
    }

    private void sendDeathMessage(Player player) {
        if(!customDeathEvent.shouldSendDeathMessage()) return;
        Component deathMessage = customDeathEvent.getDeathMessage();
        if(deathMessage == null) {
            deathMessage = Component.text(String.format("%s died.", player.getName()));
        }
        Bukkit.broadcast(deathMessage);
    }

    private void teleportPlayerToRespawnLocation(@NotNull Player player) {
        if(!customDeathEvent.shouldTeleportPlayerToRespawnLocation()) return;
        player.teleport(customDeathEvent.getRespawnLocation());
    }

    private void fullyHealPlayer(@NotNull Player player) {
        if(player.getAttribute(Attribute.MAX_HEALTH) == null) return;
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue());
    }
}
