package com.bindothorpe.champions.domain.game.map.gameObjects;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.team.TeamColor;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public record ChampionSelectGameObject(ClassType classType, BlockFace facingDirection, Vector worldLocation) implements GameObject {

    @Override
    public GameObjectType getType() {
        return GameObjectType.CHAMPION_SELECT;
    }

    @Override
    public void serialize(ConfigurationSection section) {
        section.set("classType", classType.toString());
        section.set("facingDirection", facingDirection.name());
        section.set("x", worldLocation.getX());
        section.set("y", worldLocation.getY());
        section.set("z", worldLocation.getZ());
    }

    public static GameObject deserialize(ConfigurationSection section) {
        ClassType classType = ClassType.valueOf(section.getString("classType"));
        BlockFace facingDirection = BlockFace.valueOf(section.getString("facingDirection"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        return new ChampionSelectGameObject(classType, facingDirection, new Vector(x, y, z));
    }
}
