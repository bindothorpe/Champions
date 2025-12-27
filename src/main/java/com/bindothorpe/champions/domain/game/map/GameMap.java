package com.bindothorpe.champions.domain.game.map;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;

import java.util.HashSet;
import java.util.Set;

public class GameMap {

    private String id;
    private String name;
    private SlimeWorld slimeWorld;
    private SlimeWorldInstance slimeWorldInstance;

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

    public SlimeWorld getSlimeWorld() {
        return slimeWorld;
    }

    public void setSlimeWorld(SlimeWorld slimeWorld, SlimeWorldInstance slimeWorldInstance) {
        this.slimeWorld = slimeWorld;
        this.slimeWorldInstance = slimeWorldInstance;
        saved = false;
    }

    public SlimeWorldInstance getSlimeWorldInstance() {
        return slimeWorldInstance;
    }

    public Set<GameObject> getGameObjects() {
        return gameObjects;
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
}