package com.bindothorpe.champions.domain.game.map.gameObjects;

import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.team.TeamColor;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public record SpawnPointGameObject(TeamColor team, BlockFace facingDirection, Vector worldLocation) implements GameObject {

    @Override
    public GameObjectType getType() {
        return GameObjectType.SPAWN_POINT;
    }
}
