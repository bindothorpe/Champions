package com.bindothorpe.champions.events.game.map;

import com.bindothorpe.champions.domain.game.map.GameMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerStopEditingMapEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final GameMap gameMap;
    private final boolean saved;

    public PlayerStopEditingMapEvent(Player player, GameMap gameMap, boolean saved) {
        this.player = player;
        this.gameMap = gameMap;
        this.saved = saved;
    }

    public Player getPlayer() {
        return player;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * @return true if the map was saved before stopping editing, false if changes were discarded
     */
    public boolean wasSaved() {
        return saved;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}