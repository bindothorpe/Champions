package com.bindothorpe.champions.config.game.map;

import com.bindothorpe.champions.config.CustomConfig;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.game.map.gameObjects.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapConfig extends CustomConfig {
    public MapConfig() {
        super("map_config");
    }

    public Map<String, GameMap> getAllMaps() {
        Map<String, GameMap> maps = new HashMap<>();

        ConfigurationSection mapsSection = getFile().getConfigurationSection("game_maps");

        if (mapsSection == null) {
            return maps;
        }

        for (String mapId : mapsSection.getKeys(false)) {
            GameMap gameMap = getMap(mapId);
            if (gameMap != null) {
                maps.put(mapId, gameMap);
            }
        }

        return maps;
    }

    public void saveMap(GameMap gameMap) {
        ConfigurationSection mapSection = getFile().createSection("game_maps." + gameMap.getId());

        mapSection.set("name", gameMap.getName());

        // Save game objects
        ConfigurationSection objectsSection = mapSection.createSection("objects");
        int index = 0;
        for (GameObject obj : gameMap.getGameObjects()) {
            ConfigurationSection objSection = objectsSection.createSection(String.valueOf(index));
            objSection.set("type", obj.getType().name());
            obj.serialize(objSection);
            index++;
        }

        saveFile();
        gameMap.setSaved(true);
    }

    public GameMap getMap(String id) {
        ConfigurationSection mapSection = getFile().getConfigurationSection("game_maps." + id);

        if (mapSection == null) {
            return null;
        }

        String name = mapSection.getString("name");
        GameMap gameMap = new GameMap(id, name);

        // Load game objects
        ConfigurationSection objectsSection = mapSection.getConfigurationSection("objects");
        Set<GameObject> objects = new HashSet<>();

        if (objectsSection != null) {
            for (String key : objectsSection.getKeys(false)) {
                ConfigurationSection objSection = objectsSection.getConfigurationSection(key);
                if (objSection != null) {
                    GameObjectType type = GameObjectType.valueOf(objSection.getString("type"));
                    GameObject obj = deserializeGameObject(type, objSection);
                    if (obj != null) {
                        objects.add(obj);
                    }
                }
            }
        }

        gameMap.setGameObjects(objects);
        gameMap.setSaved(true);

        return gameMap;
    }

    private GameObject deserializeGameObject(GameObjectType type, ConfigurationSection section) {
        return switch (type) {
            case SPAWN_POINT -> SpawnPointGameObject.deserialize(section);
            case CAPTURE_POINT -> CapturePointGameObject.deserialize(section);
            case GEM -> GemGameObject.deserialize(section);
            case CHEST -> ChestGameObject.deserialize(section);
            case CHAMPION_SELECT -> ChampionSelectGameObject.deserialize(section);
            default -> null;
        };
    }
}