package com.bindothorpe.champions.domain.game.mapOld;

import org.bukkit.World;

public interface IGameMap {

    boolean load();
    void unload();
    boolean reload();
    boolean isLoaded();
    World getWorld();
}
