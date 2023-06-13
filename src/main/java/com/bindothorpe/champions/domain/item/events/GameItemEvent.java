package com.bindothorpe.champions.domain.item.events;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class GameItemEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private boolean cancelled;

    private final DomainController dc;
    private final GameItem gameItem;

    public GameItemEvent(DomainController dc, GameItem gameItem) {
        this.dc = dc;
        this.gameItem = gameItem;
    }

    public GameItem getGameItem() {
        return gameItem;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
