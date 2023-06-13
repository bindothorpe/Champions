package com.bindothorpe.champions.domain.block;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class TemporaryBlockManager {

    private static TemporaryBlockManager instance;
    private final DomainController dc;

    private final Map<Location, TemporaryBlock> blocks = new HashMap<>();

    private TemporaryBlockManager(DomainController dc) {
        this.dc = dc;
    }

    public static TemporaryBlockManager getInstance(DomainController dc) {
        if(instance == null) {
            instance = new TemporaryBlockManager(dc);
        }

        return instance;
    }

    public void spawnTemporaryBlock(Location location, Material material, double duration) {
        Location l = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TemporaryBlock block = new TemporaryBlock(dc, l, material, duration);
        if(blocks.containsKey(l)) {
            block.override(blocks.get(l));
        }
        blocks.put(l, block);
        block.spawn();
    }
}
