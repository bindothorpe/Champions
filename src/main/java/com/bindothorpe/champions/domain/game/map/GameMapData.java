package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.config.CustomConfig;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.SerializationUtil;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameMapData {

    private final DomainController dc;
    private final String name;
    private final Map<String, Vector> capturePoints;
    private final Map<TeamColor, Set<Vector>> spawnPoints;
    private final Map<Vector, Vector> spawnPointDirections;

    public GameMapData(DomainController dc, String name) {
        this.dc = dc;
        this.name = name;
        this.capturePoints = new HashMap<>();
        this.spawnPoints = new HashMap<>();
        this.spawnPointDirections = new HashMap<>();
    }

    public GameMapData(DomainController dc, String name, Map<String, Vector> capturePoints, Map<TeamColor, Set<Vector>> spawnPoints, Map<Vector, Vector> spawnPointDirections) {
        this.dc = dc;
        this.name = name;
        this.capturePoints = capturePoints;
        this.spawnPoints = spawnPoints;
        this.spawnPointDirections = spawnPointDirections;
    }

    public String getName() {
        return name;
    }

    public void addCapturePoint(String name, Vector location) {
        CustomConfig config = dc.getCustomConfigManager().getConfig("map_config");

        if(config == null)
            return;

        if(config.getFile() == null)
            return;

        capturePoints.put(name, location);

        config.getFile().set("maps." + this.name + ".capturePoints." + name, SerializationUtil.vectorToString(location));
        config.saveFile();
    }

    public void addSpawnPoint(TeamColor teamColor, Vector location, Vector direction) {
        CustomConfig config = dc.getCustomConfigManager().getConfig("map_config");

        if(config == null)
            return;

        if(config.getFile() == null)
            return;

        spawnPoints.computeIfAbsent(teamColor, k -> new HashSet<>()).add(location);
        spawnPointDirections.put(location, direction);

        config.getFile().set("maps." + name + ".spawnPoints." + teamColor.name() + "." + SerializationUtil.vectorToString(location), SerializationUtil.vectorToString(direction));
        config.saveFile();
    }

    public Map<String, Vector> getCapturePoints() {
        return capturePoints;
    }

    public Map<TeamColor, Set<Vector>> getSpawnPoints() {
        return spawnPoints;
    }

    public void loadMapData(World world) {
        for(String capturePointName : capturePoints.keySet()) {
            Vector location = capturePoints.get(capturePointName);
            dc.getGameManager().addCapturePoint(new CapturePoint(dc.getGameManager(), capturePointName, location, world));
        }
    }
}
