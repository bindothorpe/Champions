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

    private GameMapManager(DomainController dc) {
        this.dc = dc;
        gameMaps = loadGameMapsFromConfig();
        editingPlayersMap = new HashMap<>();
    }

    private Map<String, GameMap> loadGameMapsFromConfig() {
        MapConfig config = (MapConfig) dc.getCustomConfigManager().getConfig("map_config");
        if (config == null)
            return new HashMap<>();

        if (config.getFile() == null)
            return new HashMap<>();

        config.reloadFile();
        return config.getAllMaps();
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

    public Set<String> getGameMapIds() {
        return gameMaps.keySet();
    }

    public @Nullable GameMap createMap(@NotNull DomainController dc, @NotNull String id, @NotNull String name) {
        if(getGameMapIds().contains(id)) return null;

        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
        SlimeWorld slimeWorld = asp.createEmptyWorld(id, false, new SlimePropertyMap(), dc.getDatabaseController().getMysqlLoader());
        GameMap map;

        try {
            asp.saveWorld(slimeWorld);
            map = new GameMap(id, name);
            gameMaps.put(id, map);

//            MapConfig config = (MapConfig) dc.getCustomConfigManager().getConfig("map_config");
//            if (config != null && config.getFile() != null) config.saveMap(map);

        } catch (IOException ignored) {
            return null;
        }

        return map;
    }

    public void saveMap(@NotNull String id) {
        //TODO: Implement saving it.
    }

    public void editMap(@NotNull DomainController dc, @NotNull Player player, @NotNull String id) throws Exception {
        if(!gameMaps.containsKey(id)) {
            throw new Exception(String.format("Map with id '%s' does not exist.", id));
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
