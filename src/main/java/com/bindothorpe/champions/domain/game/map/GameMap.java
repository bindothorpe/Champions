package com.bindothorpe.champions.domain.game.map;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GameMap {

    private String id;
    private String name;
    private @Nullable SlimeWorld slimeWorld;
    private @Nullable SlimeWorldInstance slimeWorldInstance;

    private boolean saved = false;

    private Set<GameObject> gameObjects = new HashSet<>();

    public GameMap(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return this.saved;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable SlimeWorld getSlimeWorld() {
        return slimeWorld;
    }

    public void setSlimeWorld(SlimeWorld slimeWorld, SlimeWorldInstance slimeWorldInstance) {
        this.slimeWorld = slimeWorld;
        this.slimeWorldInstance = slimeWorldInstance;
        saved = false;
    }

    public @Nullable SlimeWorldInstance getSlimeWorldInstance() {
        return slimeWorldInstance;
    }

    public Set<GameObject> getGameObjects() {
        return gameObjects;
    }

    public Set<GameObject> getGameObjectsOfType(GameObjectType type) {
        return gameObjects.stream().filter(gameObject -> gameObject.getType().equals(type)).collect(Collectors.toSet());
    }

    public void setGameObjects(Set<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
        saved = false;
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        saved = false;
    }

    public void removeGameObject(GameObject gameObject) {
        gameObjects.remove(gameObject);
        saved = false;
    }

    public static boolean isOverlappingOtherGameObject(Location location, Set<GameObject> gameObjects) {
        Vector blockVector = location.toBlockLocation().toVector();
        return gameObjects.stream().anyMatch(gameObject -> gameObject.worldLocation().toLocation(location.getWorld()).toBlockLocation().toVector().equals(blockVector));
    }

}