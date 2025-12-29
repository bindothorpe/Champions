package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.ChampionsPlugin;
import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.config.CustomConfig;
import com.bindothorpe.champions.config.game.map.MapConfig;
import com.bindothorpe.champions.events.game.map.PlayerStartEditingMapEvent;
import com.bindothorpe.champions.util.PersistenceUtil;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.*;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class GameMapManager {

    private final DomainController dc;
    private static GameMapManager instance;

    private final Map<String, GameMap> gameMaps;
    private final Map<UUID, GameMap> editingPlayersMap;

    private boolean mapsLoaded = false;

    private GameMapManager(DomainController dc) {
        this.dc = dc;
        gameMaps = new HashMap<>();
        editingPlayersMap = new HashMap<>();
        // Don't load here - let plugin call it explicitly
    }

    /**
     * Initialize and load all maps from database
     * Should be called during plugin startup
     */
    public void initialize() {
        loadGameMapsFromDatabase();
    }

    private void loadGameMapsFromDatabase() {
        mapsLoaded = false;
        try {
            dc.getDatabaseController().getGameMapService().loadAllGameMaps(loadedMaps -> {
                gameMaps.clear();
                for (GameMap map : loadedMaps) {
                    gameMaps.put(map.getId(), map);
                }
                mapsLoaded = true;
                dc.getPlugin().getLogger().info("Loaded " + loadedMaps.size() + " maps from database");
            });
        } catch (Exception e) {
            dc.getPlugin().getLogger().severe("Failed to load maps from database: " + e.getMessage());
            e.printStackTrace();
            mapsLoaded = true; // Set to true even on failure so commands don't hang
        }
    }

    /**
     * Check if maps have finished loading from database
     */
    public boolean areMapsLoaded() {
        return mapsLoaded;
    }

    /**
     * Reload all maps from the database
     */
    public void reloadMapsFromDatabase() {
        loadGameMapsFromDatabase();
    }

    public static GameMapManager getInstance(DomainController dc) {
        if(instance == null) {
            instance = new GameMapManager(dc);
        }

        return instance;
    }

    public @Nullable GameMap getEditingMapForPlayer(@NotNull UUID uuid) {
        return editingPlayersMap.get(uuid);
    }

    public @Nullable GameMap getEditingMapForPlayer(@NotNull Player player) {
        return getEditingMapForPlayer(player.getUniqueId());
    }

    /**
     * Stop editing a map for a player (removes them from the editing map)
     */
    public void stopEditingMap(@NotNull Player player) {
        editingPlayersMap.remove(player.getUniqueId());
    }

    /**
     * Stop editing a map for a player by UUID
     */
    public void stopEditingMap(@NotNull UUID uuid) {
        editingPlayersMap.remove(uuid);
    }

    public Set<String> getGameMapIds() {
        return gameMaps.keySet();
    }

    public @Nullable GameMap createMap(@NotNull DomainController dc, @NotNull String id, @NotNull String name) {
        if(getGameMapIds().contains(id)) return null;

        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
        SlimeWorld slimeWorld = asp.createEmptyWorld(id, false, new SlimePropertyMap(), dc.getDatabaseController().getMysqlLoader());
        GameMap map;

        try {
            // Save the SlimeWorld to MySQL
            asp.saveWorld(slimeWorld);

            // Create GameMap object
            map = new GameMap(id, name);
            gameMaps.put(id, map);

            // Save GameMap to database immediately
            dc.getDatabaseController().getGameMapService().saveGameMap(map, success -> {
                if (success) {
                    dc.getPlugin().getLogger().info("Successfully saved new map '" + id + "' to database");
                } else {
                    dc.getPlugin().getLogger().warning("Failed to save new map '" + id + "' to database");
                }
            });

        } catch (IOException | SQLException e) {
            dc.getPlugin().getLogger().severe("Failed to create map '" + id + "': " + e.getMessage());
            return null;
        }

        return map;
    }

    public void editMap(@NotNull DomainController dc, @NotNull Player player, @NotNull String id) throws Exception {
        if(!mapsLoaded) {
            throw new Exception("Maps are still loading from database. Please wait a moment and try again.");
        }

        if(!gameMaps.containsKey(id)) {
            throw new Exception(String.format("Map with id '%s' does not exist.", id));
        }

        // Load the map from database to get latest GameObject data
        try {
            GameMap mapFromDb = dc.getDatabaseController().getGameMapService().loadGameMapSync(id).orElse(null);
            if(mapFromDb != null) {
                gameMaps.put(id, mapFromDb);
            }
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning("Failed to load map data from database for " + id + ": " + e.getMessage());
        }

        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
        SlimeWorld slimeWorld = asp.readWorld(dc.getDatabaseController().getMysqlLoader(), id, false, new SlimePropertyMap());
        SlimeWorldInstance slimeWorldInstance = asp.loadWorld(slimeWorld, false);
        GameMap map = gameMaps.get(id);
        map.setSlimeWorld(slimeWorld, slimeWorldInstance);

        editingPlayersMap.put(player.getUniqueId(), map);
        player.teleport(slimeWorldInstance.getBukkitWorld().getSpawnLocation());

        PlayerStartEditingMapEvent editingMapEvent = new PlayerStartEditingMapEvent(player, map);
        editingMapEvent.callEvent();
    }

    public boolean isGameMapWorld(@NotNull DomainController dc, @NotNull World world) {
        return PersistenceUtil.hasData(world, getKey(dc.getPlugin()), PersistentDataType.STRING);
    }

    public @Nullable String getGameMapIdFromWorld(@NotNull DomainController dc, @NotNull World world) {
        if(!isGameMapWorld(dc, world)) return null;
        return PersistenceUtil.getData(world, getKey(dc.getPlugin()), PersistentDataType.STRING);
    }

    private NamespacedKey getKey(ChampionsPlugin plugin) {
        return new NamespacedKey(plugin, "game-map-id");
    }
}