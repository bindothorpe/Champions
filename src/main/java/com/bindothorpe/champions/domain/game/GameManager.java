package com.bindothorpe.champions.domain.game;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePointManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.game.GameStateChangeEvent;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

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
        setGameState(newState);
    }

    public void setGameState(GameState state) throws IllegalArgumentException {
        //Send broadcast message
        ChatUtil.sendGameBroadcast(ChatUtil.Prefix.PLUGIN,
                Component.text(gameState.name()).color(NamedTextColor.GRAY)
                .append(Component.text(" -> ").color(NamedTextColor.GRAY))
                .append(Component.text(state.name()).color(NamedTextColor.YELLOW)));

        //Handle state change
        switch (state) {
            case LOBBY -> handleLobbyCase();
            case LOBBY_COUNTDOWN -> handleLobbyCountdownCase();
            case GAME_START_COUNTDOWN -> handleGameStartCountdownCase();
            case IN_PROGRESS -> handleInProgressCase();
            case GAME_END_COUNTDOWN -> handleGameEndCountdownCase();
        }

        //Call event
        Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(gameState, state));
        //Set new state
        gameState = state;
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


    private void handleLobbyCase() {
        //Check if the previous state is Lobby Countdown
        if (gameState != GameState.GAME_END_COUNTDOWN)
            throw new IllegalArgumentException("Cannot go back to lobby if you are not in game end countdown.");
    }
    private void handleLobbyCountdownCase() {
        //Check if the previous state is Lobby
        if (gameState != GameState.LOBBY)
            throw new IllegalArgumentException("Cannot start countdown if you are not in lobby.");

        //Check if the map is loaded
        if (!getDc().getGameMapManager().isLoaded())
            throw new IllegalArgumentException("No map selected, please select a map first.");

        //Set the game state to Lobby Countdown
        ChatUtil.sendCountdown(dc, new ArrayList<>(Bukkit.getOnlinePlayers()), 10, "Teleporting in %s seconds", () -> {
            setGameState(GameState.GAME_START_COUNTDOWN);
        });
    }

    private void handleGameStartCountdownCase() {
        //Check if the previous state is Lobby Countdown
        if (gameState != GameState.LOBBY_COUNTDOWN)
            throw new IllegalArgumentException("Cannot start countdown if you are not in lobby countdown.");

        //Check if the map is loaded
        if (!getDc().getGameMapManager().isLoaded())
            throw new IllegalArgumentException("No map selected, please select a map first.");

        //Teleport all players to the map
        dc.getGameMapManager().teleportAllToMap(new ArrayList<>(Bukkit.getOnlinePlayers()));

        //Add root status effect to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            dc.addStatusEffectToEntity(StatusEffectType.ROOT, player.getUniqueId(), 10);
        }

        //Start the countdown
        ChatUtil.sendCountdown(dc, new ArrayList<>(Bukkit.getOnlinePlayers()), 10, "Starting in %s seconds",
                () -> {
                    setGameState(GameState.IN_PROGRESS);
                });
    }

    private void handleInProgressCase() {
        //Check if the previous state is Game Start Countdown
        if (gameState != GameState.GAME_START_COUNTDOWN)
            throw new IllegalArgumentException("Cannot start game if you are not in game start countdown.");

    }


    private void handleGameEndCountdownCase() {
        //Check if the previous state is In Progress
        if (gameState != GameState.IN_PROGRESS)
            throw new IllegalArgumentException("Cannot start countdown if you are not in game.");

        new BukkitRunnable() {
            @Override
            public void run() {
                setGameState(GameState.LOBBY);
                dc.getGameMapManager().unloadMap();
            }
        }.runTaskLater(dc.getPlugin(), 20 * 10);
    }
}
