package com.bindothorpe.champions.events.interact;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerClickEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final LivingEntity clickedEntity;

    public PlayerClickEvent(Player player, LivingEntity clickedEntity) {
        this.player = player;
        this.clickedEntity = clickedEntity;
    }
    public PlayerClickEvent(Player player) {
        this(player, null);
    }

    public boolean isSword() {
        return player.getInventory().getItemInMainHand().getType().toString().contains("SWORD");
    }

    public boolean isAxe() {
        return player.getInventory().getItemInMainHand().getType().toString().contains("_AXE");
    }

    public boolean isBow() {
        return player.getInventory().getItemInMainHand().getType().toString().contains("BOW");
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

    public LivingEntity getClickedEntity() {
        return clickedEntity;
    }
}
