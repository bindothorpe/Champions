package com.bindothorpe.champions.events.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerDropItemWrapperEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final ItemStack item;

    public PlayerDropItemWrapperEvent(Player player, ItemStack item) {
        this.player = player;
        this.item = item;
    }

    public boolean isSword() {
        return item.getType().toString().contains("SWORD");
    }

    public boolean isAxe() {
        return item.getType().toString().contains("_AXE");
    }

    public boolean isBow() {
        return item.getType().toString().contains("BOW");
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

}
