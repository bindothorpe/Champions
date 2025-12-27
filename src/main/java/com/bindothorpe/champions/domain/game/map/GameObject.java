package com.bindothorpe.champions.domain.game.map;

import org.bukkit.util.Vector;

public interface GameObject {

    GameObjectType getType();
    Vector worldLocation();
}
