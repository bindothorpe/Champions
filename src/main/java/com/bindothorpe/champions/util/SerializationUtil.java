package com.bindothorpe.champions.util;

import org.bukkit.util.Vector;

public class SerializationUtil {


    public static String vectorToString(Vector vector) {
        return (vector.getX() + "," + vector.getY() + "," + vector.getZ()).replaceAll("\\.", "_");
    }

    public static Vector stringToVector(String string) {
        String[] split = string.replaceAll("_", ".").split(",");
        return new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }
}
