package com.bindothorpe.champions.events.game;

import com.bindothorpe.champions.domain.game.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStateChangeEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final GameState oldState;
    private final GameState newState;

    public GameStateChangeEvent(GameState oldState, GameState newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    public GameState getOldState() {
        return oldState;
    }

    public GameState getNewState() {
        return newState;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
