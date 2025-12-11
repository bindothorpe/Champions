package com.bindothorpe.champions.events.interact.blocking;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerStopBlockingEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final double blockDuration;
    private final long blockDurationInMilliseconds;

    public PlayerStopBlockingEvent(Player player, double blockDuration, long blockDurationInMilliseconds) {
        this.player = player;
        this.blockDuration = blockDuration;
        this.blockDurationInMilliseconds = blockDurationInMilliseconds;
    }

    public boolean isSword() {
        return player.getInventory().getItemInMainHand().getType().toString().contains("SWORD");
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

    public double getBlockDuration() {
        return blockDuration;
    }

    public long getBlockDurationInMilliseconds() {
        return blockDurationInMilliseconds;
    }
}
