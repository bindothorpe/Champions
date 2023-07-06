package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.config.CustomConfig;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        if(gameMapDataMap.isEmpty()) {
            loadAllMapsFromConfig();
        }

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
        mapData.loadMapData(map.getWorld());
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

        GameMapData data = getGameMapData(map.getName());

        if(data == null)
            return;

        map.unload();
        data.unloadMapData();
        map = null;
    }

    private void loadAllMapsFromConfig() {
        CustomConfig config = dc.getCustomConfigManager().getConfig("map_config");
        if(config == null)
            return;

        if(config.getFile() == null)
            return;

        config.reloadFile();

        String defaultPath = "maps";

        for(String path : config.getFile().getConfigurationSection(defaultPath).getKeys(false)) {
            path = defaultPath + "." + path;
            String name = config.getFile().getString(path + ".name");
            Map<String, Vector> capturePoints = new HashMap<>();
            Map<TeamColor, Set<Vector>> spawnPoints = new HashMap<>();
            Map<Vector, Vector> spawnPointDirection = new HashMap<>();

            for(String capturePoint : config.getFile().getConfigurationSection(path + ".capturePoints").getKeys(false)) {
                String capturePointPath = path + ".capturePoints." + capturePoint;

                String capturePointVectorAsString = config.getFile().getString(capturePointPath);
                if(capturePointVectorAsString == null)
                    continue;
                capturePoints.put(capturePoint, SerializationUtil.stringToVector(capturePointVectorAsString));
            }

            for(String spawnpointTeam : config.getFile().getConfigurationSection(path + ".spawnPoints").getKeys(false)) {
                String spawnpointTeamPath = path + ".spawnPoints." + spawnpointTeam;
                TeamColor teamColor = TeamColor.valueOf(spawnpointTeam.toUpperCase());
                Set<Vector> spawnpointVectors = new HashSet<>();

                for(String spawnpoint : config.getFile().getConfigurationSection(spawnpointTeamPath).getKeys(false)) {
                    String spawnpointPath = spawnpointTeamPath + "." + spawnpoint;
                    String spawnpointVectorAsString = config.getFile().getString(spawnpointPath);
                    if(spawnpointVectorAsString == null)
                        continue;

                    Vector spawnPoint = SerializationUtil.stringToVector(spawnpoint);
                    Vector direction = SerializationUtil.stringToVector(spawnpointVectorAsString);
                    spawnpointVectors.add(spawnPoint);
                    spawnPointDirection.put(spawnPoint, direction);
                }

                spawnPoints.put(teamColor, spawnpointVectors);
            }

            GameMapData gameMapData = new GameMapData(dc, name, capturePoints, spawnPoints, spawnPointDirection);
            gameMapDataMap.put(name, gameMapData);
            System.out.println("Loaded map " + name + " from config");
        }
    }
}
