package com.bindothorpe.champions.util;

import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class ShapeUtil {

    private static final int DEVIDER = 32;

    public static Set<Vector> sphere(double radius) {
        Set<Vector> set = new HashSet<>();
        for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / DEVIDER) {
            for (double phi = 0; phi <= Math.PI; phi += Math.PI / DEVIDER) {
                double x = radius * Math.cos(theta) * Math.sin(phi);
                double y = radius * Math.cos(phi) + 2;
                double z = radius * Math.sin(theta) * Math.sin(phi);
                set.add(new Vector(Math.floor(x), Math.floor(y), Math.floor(z)));
            }
        }

        return set;
    }

}
