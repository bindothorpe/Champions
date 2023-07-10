package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.config.CustomConfig;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GameMapManager {

    private static GameMapManager instance;
    private final DomainController dc;
    private static File gameMapsFolder;
    private GameMap map;

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
        if (gameMapsFolder == null) {
            dc.getPlugin().getDataFolder().mkdirs();
            gameMapsFolder = new File(dc.getPlugin().getDataFolder(), "gameMaps");
            if (!gameMapsFolder.exists()) gameMapsFolder.mkdirs();
        }
        return gameMapsFolder;
    }

    public boolean createGameMap(String name) {

        if (gameMapDataMap.containsKey(name)) {
            return false;
        }

        CustomConfig config = dc.getCustomConfigManager().getConfig("map_config");

        if (config == null) {
            return false;
        }

        if (config.getFile() == null)
            return false;

        gameMapDataMap.put(name, new GameMapData(dc, name));

        config.getFile().set("maps." + name + ".name", name);
        dc.getCustomConfigManager().getConfig("map_config").saveFile();
        return true;
    }

    public GameMapData getGameMapData(String mapName) {
        return gameMapDataMap.get(mapName);
    }

    public List<String> getGameMapNames() {
        if (gameMapDataMap.isEmpty())
            loadAllMapsFromConfig();
        return gameMapDataMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    public boolean loadMap(String mapName) {
        if (gameMapDataMap.isEmpty()) {
            loadAllMapsFromConfig();
        }

        if (!gameMapDataMap.containsKey(mapName))
            return false;

        return loadMap(gameMapDataMap.get(mapName));
    }

    public boolean loadMap(GameMapData mapData) {

        if (mapData == null)
            return false;

        if (map != null) {
            map.unload();
            map = null;
        }

        map = new GameMap(dc.getGameMapManager().getGameMapsFolder(), mapData.getName(), true);
        mapData.load(map.getWorld());
        return true;
    }

    public void teleportAllToMap(Collection<Player> players) {
        teleportAllToMap(players, map.getName());
    }


    public void teleportAllToMap(Collection<Player> players, String mapName) {
        Objects.requireNonNull(players, "Players passed cannot be null");
        Objects.requireNonNull(mapName, "Map name cannot be null");

        if (mapName.isEmpty()) {
            throw new IllegalArgumentException("Map name cannot be empty");
        }

        Objects.requireNonNull(map, String.format("Map is not loaded: %s", mapName));

        GameMapData gameMapData = gameMapDataMap.get(mapName);
        Map<TeamColor, Set<Vector>> spawnPointsSet = gameMapData.getSpawnPoints();
        Map<Vector, Vector> spawnPointDirections = gameMapData.getSpawnPointDirections();

        if (spawnPointsSet.isEmpty() || spawnPointDirections.isEmpty()) {
            throw new IllegalArgumentException(String.format("Map does not have any spawnpoints: %s", mapName));
        }

        Map<TeamColor, List<Vector>> spawnPointsList = new HashMap<>();
        Map<TeamColor, Integer> teamCounter = new HashMap<>();

        for (TeamColor team : spawnPointsSet.keySet()) {
            spawnPointsList.put(team, new ArrayList<>(spawnPointsSet.get(team)));
            teamCounter.put(team, 0);
        }

        for (Player player : players) {
            TeamColor team = Objects.requireNonNull(dc.getTeamFromEntity(player), String.format("Player is not on a team: %s", player.getName()));
            int counter = teamCounter.get(team);

            Vector spawnPoint = spawnPointsList.get(team).get(counter % spawnPointsList.get(team).size());
            Location location = new Location(map.getWorld(), spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
            location.setDirection(spawnPointDirections.get(spawnPoint));

            player.teleport(location);
            teamCounter.put(team, counter + 1);
        }
    }


    public void teleportToMap(Player player, String mapName) throws IllegalArgumentException {
        if (player == null) {
            throw new IllegalArgumentException("Player is null");
        }

        if (dc.getTeamFromEntity(player) == null) {
            throw new IllegalArgumentException("Player is not on a team");
        }

        TeamColor team = dc.getTeamFromEntity(player);

        if (map == null) {
            throw new IllegalArgumentException(String.format("Map is not loaded: %s", mapName));
        }

        Map<TeamColor, Set<Vector>> spawnPoints = gameMapDataMap.get(mapName).getSpawnPoints();

        if (spawnPoints.isEmpty()) {
            throw new IllegalArgumentException(String.format("Map does not have any spawnpoints: %s", mapName));
        }

        Set<Vector> teamSpawnPoints = spawnPoints.get(team);

        if (teamSpawnPoints.isEmpty()) {
            throw new IllegalArgumentException(String.format("Map does not have any spawnpoints for team %s: %s", team, mapName));
        }

        Map<Vector, Vector> spawnPointDirections = gameMapDataMap.get(mapName).getSpawnPointDirections();

        if (spawnPointDirections.isEmpty()) {
            throw new IllegalArgumentException(String.format("Map does not have any spawnpoint directions: %s", mapName));
        }


        if (map.getWorld() == null) {
            throw new IllegalArgumentException(String.format("Map world is null: %s", mapName));
        }

        Vector spawnPoint = teamSpawnPoints.stream().findAny().orElse(null);

        if (spawnPoint == null) {
            throw new IllegalArgumentException(String.format("Map does not have any spawnpoints for team %s: %s", team, mapName));
        }

        Vector spawnPointDirection = spawnPointDirections.get(spawnPoint);

        Location loc = new Location(map.getWorld(), spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        loc.setDirection(spawnPointDirection);
        player.teleport(loc);
    }

    public void unloadMap() {
        if (map == null)
            return;

        GameMapData data = getGameMapData(map.getName());

        if (data == null)
            return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(map.getWorld())) {
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            }
        }

        map.unload();
        data.unload();
        map = null;
    }

    private void loadAllMapsFromConfig() {
        CustomConfig config = dc.getCustomConfigManager().getConfig("map_config");
        if (config == null)
            return;

        if (config.getFile() == null)
            return;

        config.reloadFile();

        String defaultPath = "maps";

        for (String path : config.getFile().getConfigurationSection(defaultPath).getKeys(false)) {
            path = defaultPath + "." + path;
            String name = config.getFile().getString(path + ".name");
            Map<String, Vector> capturePoints = new HashMap<>();
            Map<TeamColor, Set<Vector>> spawnPoints = new HashMap<>();
            Map<Vector, Vector> spawnPointDirection = new HashMap<>();

            for (String capturePoint : config.getFile().getConfigurationSection(path + ".capturePoints").getKeys(false)) {
                String capturePointPath = path + ".capturePoints." + capturePoint;

                String capturePointVectorAsString = config.getFile().getString(capturePointPath);
                if (capturePointVectorAsString == null)
                    continue;
                capturePoints.put(capturePoint, SerializationUtil.stringToVector(capturePointVectorAsString));
            }

            for (String spawnpointTeam : config.getFile().getConfigurationSection(path + ".spawnPoints").getKeys(false)) {
                String spawnpointTeamPath = path + ".spawnPoints." + spawnpointTeam;
                TeamColor teamColor = TeamColor.valueOf(spawnpointTeam.toUpperCase());
                Set<Vector> spawnpointVectors = new HashSet<>();

                for (String spawnpoint : config.getFile().getConfigurationSection(spawnpointTeamPath).getKeys(false)) {
                    String spawnpointPath = spawnpointTeamPath + "." + spawnpoint;
                    String spawnpointVectorAsString = config.getFile().getString(spawnpointPath);
                    if (spawnpointVectorAsString == null)
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
        }
    }


    public boolean isLoaded() {
        return map != null && map.isLoaded();
    }
}
