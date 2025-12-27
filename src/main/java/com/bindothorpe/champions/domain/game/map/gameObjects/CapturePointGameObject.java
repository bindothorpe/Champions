package com.bindothorpe.champions.domain.game.map.gameObjects;

import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public record CapturePointGameObject(String name, Vector worldLocation) implements GameObject {

    @Override
    public GameObjectType getType() {
        return GameObjectType.CAPTURE_POINT;
    }

    @Override
    public void serialize(ConfigurationSection section) {
        section.set("name", name);
        section.set("x", worldLocation.getX());
        section.set("y", worldLocation.getY());
        section.set("z", worldLocation.getZ());
    }

    public static GameObject deserialize(ConfigurationSection section) {
        String name = section.getString("name");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        return new CapturePointGameObject(name, new Vector(x, y, z));
    }


}
