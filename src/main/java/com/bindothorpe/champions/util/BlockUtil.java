package com.bindothorpe.champions.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BlockUtil {

    public static Set<Block> getNearbyBlocks(Location location, double radius) {
        Set<Block> blocks = new HashSet<>();

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    Block block = location.clone().add(x * radius, y * radius, z * radius).getBlock();
                    if(!block.getType().isSolid())
                        continue;
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }
}
