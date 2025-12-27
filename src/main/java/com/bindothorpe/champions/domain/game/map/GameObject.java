package com.bindothorpe.champions.domain.game.map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public interface GameObject {

    GameObjectType getType();
    Vector worldLocation();

    /**
     * Serializes this GameObject to a ConfigurationSection
     */
    void serialize(ConfigurationSection section);

    /**
     * Deserializes a GameObject from a ConfigurationSection
     * This should be implemented as a static method in each implementing class
     */
//     static GameObject deserialize(ConfigurationSection section);
}