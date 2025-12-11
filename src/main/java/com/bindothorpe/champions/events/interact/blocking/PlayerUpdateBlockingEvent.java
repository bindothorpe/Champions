package com.bindothorpe.champions.events.interact.blocking;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerUpdateBlockingEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Player player;
    private final double blockDuration;
    private final long blockDurationInMilliseconds;

    public PlayerUpdateBlockingEvent(Player player, double blockDuration, long blockDurationInMilliseconds) {
        this.player = player;
        this.blockDuration = blockDuration;
        this.blockDurationInMilliseconds = blockDurationInMilliseconds;
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
