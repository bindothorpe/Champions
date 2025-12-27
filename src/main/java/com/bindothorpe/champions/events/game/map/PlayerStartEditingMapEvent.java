package com.bindothorpe.champions.events.game.map;

import com.bindothorpe.champions.domain.game.map.GameMap;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerStartEditingMapEvent extends PlayerEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final GameMap gameMap;

    public PlayerStartEditingMapEvent(@NotNull Player player, GameMap gameMap) {
        super(player);
        this.gameMap = gameMap;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public GameMap getGameMap() {
        return gameMap;
    }
}
