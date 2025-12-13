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

    /**
     * Creates a circle outline at Y=0 with default settings (divider=32, floor=true)
     *
     * @param radius the radius of the circle
     * @return a set of vectors representing points on the circle outline
     */
    public static Set<Vector> circle(double radius) {
        return circle(radius, false, DEVIDER, true);
    }

    /**
     * Creates a circle at Y=0 with custom fill and density settings (floor=true)
     *
     * @param radius the radius of the circle
     * @param filled if true, fills the entire circle; if false, only the outline
     * @param divider controls point density - higher values create more points
     * @return a set of vectors representing points on the circle
     */
    public static Set<Vector> circle(double radius, boolean filled, int divider) {
        return circle(radius, filled, divider, true);
    }

    /**
     * Creates a circle at Y=0 positioned at the origin with full control over all parameters
     *
     * @param radius the radius of the circle
     * @param filled if true, fills the entire circle; if false, only the outline
     * @param divider controls point density - higher values create more points
     * @param floor if true, applies Math.floor() to coordinates; if false, keeps precise decimal values
     * @return a set of vectors representing points on the circle
     */
    public static Set<Vector> circle(double radius, boolean filled, int divider, boolean floor) {
        Set<Vector> set = new HashSet<>();

        if (filled) {
            // Fill the entire circle
            double step = 1.0 / divider;
            for (double x = -radius; x <= radius; x += step) {
                for (double z = -radius; z <= radius; z += step) {
                    if (x * x + z * z <= radius * radius) {
                        if (floor) {
                            set.add(new Vector(Math.floor(x), 0, Math.floor(z)));
                        } else {
                            set.add(new Vector(x, 0, z));
                        }
                    }
                }
            }
        } else {
            // Just the outline
            for (int i = 0; i < divider; i++) {
                double angle = 2 * Math.PI * i / divider;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);

                if (floor) {
                    set.add(new Vector(Math.floor(x), 0, Math.floor(z)));
                } else {
                    set.add(new Vector(x, 0, z));
                }
            }
        }

        return set;
    }

    /**
     * Creates a 2D cone (sector) at Y=0 with default minRange=0 and default divider
     *
     * @param maxRange the maximum range/radius of the cone
     * @param angleInDegrees the angle of the cone in degrees
     * @param direction the direction vector the cone should face
     * @return a set of vectors representing points within the cone area
     */
    public static Set<Vector> cone2D(double maxRange, double angleInDegrees, Vector direction) {
        return cone2D(0, maxRange, angleInDegrees, direction, DEVIDER);
    }

    /**
     * Creates a 2D cone (sector) at Y=0 with specified min and max range and default divider
     *
     * @param minRange the minimum range/radius of the cone (creates donut effect)
     * @param maxRange the maximum range/radius of the cone
     * @param angleInDegrees the angle of the cone in degrees
     * @param direction the direction vector the cone should face
     * @return a set of vectors representing points within the cone area
     */
    public static Set<Vector> cone2D(double minRange, double maxRange, double angleInDegrees, Vector direction) {
        return cone2D(minRange, maxRange, angleInDegrees, direction, DEVIDER);
    }

    /**
     * Creates a 2D cone (sector) at Y=0 with custom density
     *
     * @param maxRange the maximum range/radius of the cone
     * @param angleInDegrees the angle of the cone in degrees
     * @param direction the direction vector the cone should face
     * @param divider controls point density - higher values create more points
     * @return a set of vectors representing points within the cone area
     */
    public static Set<Vector> cone2D(double maxRange, double angleInDegrees, Vector direction, int divider) {
        return cone2D(0, maxRange, angleInDegrees, direction, divider);
    }

    /**
     * Creates a 2D cone (sector) at Y=0 with specified min and max range and custom density
     *
     * @param minRange the minimum range/radius of the cone (creates donut effect)
     * @param maxRange the maximum range/radius of the cone
     * @param angleInDegrees the angle of the cone in degrees
     * @param direction the direction vector the cone should face
     * @param divider controls point density - higher values create more points
     * @return a set of vectors representing points within the cone area
     */
    public static Set<Vector> cone2D(double minRange, double maxRange, double angleInDegrees, Vector direction, int divider) {
        Set<Vector> set = new HashSet<>();

        // Normalize direction vector and get the yaw angle
        Vector normalizedDirection = direction.clone().normalize();
        double baseAngle = Math.atan2(normalizedDirection.getZ(), normalizedDirection.getX());

        // Convert cone angle from degrees to radians and get half-angle
        double halfAngleRad = Math.toRadians(angleInDegrees / 2.0);

        // Fill the cone area
        double step = 1.0 / divider;
        for (double x = -maxRange; x <= maxRange; x += step) {
            for (double z = -maxRange; z <= maxRange; z += step) {
                double distance = Math.sqrt(x * x + z * z);

                // Check if point is within range bounds
                if (distance < minRange || distance > maxRange) {
                    continue;
                }

                // Calculate angle from base direction to this point
                double pointAngle = Math.atan2(z, x);
                double angleDiff = Math.abs(pointAngle - baseAngle);

                // Handle angle wrapping (e.g., difference between -179° and 179°)
                if (angleDiff > Math.PI) {
                    angleDiff = 2 * Math.PI - angleDiff;
                }

                // Check if point is within the cone angle
                if (angleDiff <= halfAngleRad) {
                    set.add(new Vector(x, 0, z)); // Keep precise decimal positions
                }
            }
        }

        return set;
    }

    /**
     * Creates a line between two points with custom density
     *
     * @param start the starting point
     * @param end the ending point
     * @param step the distance between points (smaller = more dense)
     * @return a set of vectors representing points along the line
     */
    public static Set<Vector> line(Vector start, Vector end, double step) {
        Set<Vector> set = new HashSet<>();

        Vector direction = end.clone().subtract(start);
        double distance = direction.length();

        if (distance == 0) {
            set.add(start.clone());
            return set;
        }

        direction.normalize();

        // Add points along the line
        for (double i = 0; i <= distance; i += step) {
            Vector point = start.clone().add(direction.clone().multiply(i));
            set.add(point);
        }

        // Always include the end point
        set.add(end.clone());

        return set;
    }
}