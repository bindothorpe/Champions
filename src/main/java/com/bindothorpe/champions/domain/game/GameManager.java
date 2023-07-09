package com.bindothorpe.champions.domain.game;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePointManager;
import com.bindothorpe.champions.events.game.GameStateChangeEvent;
import org.bukkit.Bukkit;

public class GameManager {

    private static GameManager instance;
    private final DomainController dc;
    private final CapturePointManager capturePointManager = CapturePointManager.getInstance();
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

    public boolean addCapturePoint(CapturePoint capturePoint) {
        return capturePointManager.addCapturePoint(capturePoint);
    }

    public void removeCapturePoint(String name) {
        capturePointManager.removeCapturePoint(name);
    }

    public static GameManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new GameManager(dc);
        }
        return instance;
    }

    public DomainController getDc() {
        return dc;
    }

}
