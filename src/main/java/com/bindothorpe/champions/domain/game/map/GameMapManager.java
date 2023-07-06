package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.config.CustomConfig;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GameMapManager {

    private static GameMapManager instance;
    private final DomainController dc;
    private static File gameMapsFolder;
    private LocalGameMap map;

    private final Map<String, GameMapData> gameMapDataMap = new HashMap<>();

    private GameMapManager(DomainController dc) {
        this.dc = dc;
    }

    public static GameMapManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new GameMapManager(dc);
        }
        return instance;
    }

    public File getGameMapsFolder() {
        if(gameMapsFolder == null) {
            dc.getPlugin().getDataFolder().mkdirs();
            gameMapsFolder = new File(dc.getPlugin().getDataFolder(), "gameMaps");
            if(!gameMapsFolder.exists()) gameMapsFolder.mkdirs();
        }
        return gameMapsFolder;
    }

    public boolean createGameMap(String name) {
        if(gameMapDataMap.containsKey(name)) {
            return false;
        }

        CustomConfig config = dc.getCustomConfigManager().getConfig("map_config");

        if(config == null) {
            return false;
        }

        if(config.getFile() == null)
            return false;

        gameMapDataMap.put(name, new GameMapData(dc, name));

        config.getFile().set("maps." + name + ".name", name);
        dc.getCustomConfigManager().getConfig("map_config").saveFile();
        return true;
    }

    public GameMapData getGameMapData(String mapName) {
        return gameMapDataMap.get(mapName);
    }

    public boolean loadMap(String mapName) {
        if(!gameMapDataMap.containsKey(mapName))
            return false;

        return loadMap(gameMapDataMap.get(mapName));
    }

    public boolean loadMap(GameMapData mapData) {

        if(mapData == null)
            return false;

        if(map != null) {
            map.unload();
            map = null;
        }

        map = new LocalGameMap(dc.getGameMapManager().getGameMapsFolder(), mapData.getName(), true);
        return true;
    }

    public void tpToMap(Player player, String mapName) {

        if(map == null) {
            player.sendMessage("Map does not exist: " + mapName);
            return;
        }

        Vector spawnVector = gameMapDataMap.get(mapName).getCapturePoints().values().stream().findFirst().orElse(null);
        if(spawnVector == null) {
            player.sendMessage("Map does not have a spawn point: " + mapName);
            return;
        }

        if(map.getWorld() == null) {
            player.sendMessage("Map world does not exist: " + mapName);
            return;
        }

        Location loc = new Location(map.getWorld(), spawnVector.getX(), spawnVector.getY(), spawnVector.getZ());

        player.teleport(loc);
    }

    public void unloadMap() {
        if(map == null)
            return;

        map.unload();
        map = null;
    }
}
