package com.bindothorpe.champions.domain.game;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.game.GameStateChangeEvent;
import org.bukkit.Bukkit;

public class GameManager {

    private static GameManager instance;
    private final DomainController dc;
    private GameState gameState = GameState.LOBBY;

    private GameManager(DomainController dc) {
        this.dc = dc;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setNextGameState() {
        GameState newState = gameState.getNextState();
        Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(gameState, newState));
        gameState = newState;
    }

    public static GameManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new GameManager(dc);
        }
        return instance;
    }

}
