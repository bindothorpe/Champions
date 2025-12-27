package com.bindothorpe.champions.domain.game.map.gameObjects;

import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import org.bukkit.util.Vector;

public record ChestGameObject(Vector worldLocation) implements GameObject {

    @Override
    public GameObjectType getType() {
        return GameObjectType.CHEST;
    }
}
