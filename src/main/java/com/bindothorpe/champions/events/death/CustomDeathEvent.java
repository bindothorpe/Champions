package com.bindothorpe.champions.events.death;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomDeathEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private Location respawnLocation;
    private boolean teleportPlayerToRespawnLocation = true;
    private boolean sendDeathMessage = true;
    private Component deathMessage;

    public CustomDeathEvent(@NotNull Player player) {
        this.player = player;
        this.respawnLocation = player.getRespawnLocation();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public @NotNull Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(@NotNull Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    public boolean shouldTeleportPlayerToRespawnLocation() {
        return teleportPlayerToRespawnLocation;
    }

    public void setShouldTeleportPlayerToRespawnLocation(boolean teleportPlayerToRespawnLocation) {
        this.teleportPlayerToRespawnLocation = teleportPlayerToRespawnLocation;
    }

    public boolean shouldSendDeathMessage() {
        return sendDeathMessage;
    }

    public void setShouldSendDeathMessage(boolean sendDeathMessage) {
        this.sendDeathMessage = sendDeathMessage;
    }

    public Component getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(Component deathMessage) {
        this.deathMessage = deathMessage;
    }
}
