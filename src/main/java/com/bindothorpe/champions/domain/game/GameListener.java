package com.bindothorpe.champions.domain.game;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.game.GameStateChangeEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameListener implements Listener {

    private Map<UUID, Vector> gameStartCountdownLocations = new HashMap<>();

    private final DomainController dc;

    public GameListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onGameStartCountdown(GameStateChangeEvent event) {
        if(!event.getNewState().equals(GameState.GAME_START_COUNTDOWN))
            return;

        gameStartCountdownLocations.clear();

        //TODO: Teleport all players to the correct location

        for(Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            gameStartCountdownLocations.put(uuid, player.getLocation().toVector());
            new BukkitRunnable() {
                @Override
                public void run() {
                    gameStartCountdownLocations.clear();
                    dc.setNextGameState();
                }
            }.runTaskLater(dc.getPlugin(), 5 * 20L);
        }

    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK))
            return;

        if(dc.getGameState().equals(GameState.GAME_START_COUNTDOWN)) {
            for(Map.Entry<UUID, Vector> entry : gameStartCountdownLocations.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if(player == null)
                    continue;

                Vector vector = entry.getValue();
                Location location = player.getLocation();

                if (location.getX() != vector.getX() || location.getY() != vector.getY() || location.getZ() != vector.getZ()) {
                    location.setX(vector.getX());
                    location.setY(vector.getY());
                    location.setZ(vector.getZ());
                    player.teleport(location);
                }
            }
        }

    }
}
