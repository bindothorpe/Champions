package com.bindothorpe.champions.domain.game.map.gameObjects;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public record ChestGameObject(Vector worldLocation) implements GameObject {

    @Override
    public GameObjectType getType() {
        return GameObjectType.CHEST;
    }


    @Override
    public void serialize(ConfigurationSection section) {
        section.set("x", worldLocation.getX());
        section.set("y", worldLocation.getY());
        section.set("z", worldLocation.getZ());
    }

    public static GameObject deserialize(ConfigurationSection section) {
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        return new ChestGameObject(new Vector(x, y, z));
    }
}
