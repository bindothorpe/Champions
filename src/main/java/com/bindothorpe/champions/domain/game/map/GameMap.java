package com.bindothorpe.champions.domain.game.map;

import java.util.Set;

public class GameMap {

    private String id;
    private String name;

    private boolean saved = false;

    private Set<GameObject> gameObjects;

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
}
