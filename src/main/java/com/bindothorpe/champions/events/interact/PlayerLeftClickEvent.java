package com.bindothorpe.champions.events.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLeftClickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    private final Player player;

    public PlayerLeftClickEvent(Player player) {
        this.player = player;
    }

    public boolean isSword() {
        return player.getInventory().getItemInMainHand().getType().toString().contains("SWORD");
    }

    public boolean isAxe() {
        return player.getInventory().getItemInMainHand().getType().toString().contains("_AXE");
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}