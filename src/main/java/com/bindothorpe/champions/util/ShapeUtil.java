package com.bindothorpe.champions.util;

import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class ShapeUtil {

    private static final int DEVIDER = 32;

    public static Set<Vector> sphere(double radius) {
        return sphere(radius, true, 2, DEVIDER);
    }

    public static Set<Vector> sphere(double radius, boolean floor, double yAddition, int devider) {
        Set<Vector> set = new HashSet<>();
        for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / devider) {
            for (double phi = 0; phi <= Math.PI; phi += Math.PI / devider) {
                double x = radius * Math.cos(theta) * Math.sin(phi);
                double y = radius * Math.cos(phi) + yAddition;
                double z = radius * Math.sin(theta) * Math.sin(phi);
                if(floor) {
                    set.add(new Vector(Math.floor(x), Math.floor(y), Math.floor(z)));
                } else {
                    set.add(new Vector(x, y, z));
                }
            }
        }

        return set;
    }

}
