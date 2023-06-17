package com.bindothorpe.champions.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BlockUtil {

    private static final boolean DEBUG = false;

    public static Set<Block> getNearbyBlocks(Location location, double radius) {
        return getNearbyBlocks(location, 1, radius, radius, radius);
    }

    public static Set<Block> getNearbyBlocks(Location location, double increment, double xRadius, double yRadius, double zRadius) {
        Set<Block> blocks = new HashSet<>();

        for(double x = -increment; x <= increment; x += increment) {
            for(double y = -increment; y <= increment; y += increment) {
                for(double z = -increment; z <= increment; z += increment) {
                    Location newLoc = location.clone().add(x * (xRadius / increment), y * (yRadius / increment), z * (zRadius / increment));
                    if(DEBUG) {
                        location.getWorld().spawnParticle(Particle.SMALL_FLAME, newLoc, 1, 0, 0, 0, 0, null, true);
                    }
                    Block block = newLoc.getBlock();
                    if(!block.getType().isSolid())
                        continue;
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }
}
