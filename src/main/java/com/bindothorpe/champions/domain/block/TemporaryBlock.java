package com.bindothorpe.champions.domain.block;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TemporaryBlock {

    private final DomainController dc;
    private final Location location;
    private final Material material;
    private Material oldMaterial;
    private BlockData oldBlockData;
    private final double duration;
    private BukkitTask task;

    public TemporaryBlock(DomainController dc, Location location, Material material, double duration) {
        this.dc = dc;
        this.location = location;
        this.material = material;
        this.duration = duration;
    }

    public void spawn() {
        if (oldMaterial == null)
            oldMaterial = location.getBlock().getType();

        if (oldBlockData == null)
            oldBlockData = location.getBlock().getBlockData();

        location.getBlock().setType(material);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                location.getBlock().setType(oldMaterial);
                location.getBlock().setBlockData(oldBlockData);
            }
        }.runTaskLater(dc.getPlugin(), (long) (duration * 20));
    }

    public TemporaryBlock override(TemporaryBlock temp) {
        temp.task.cancel();
        this.oldMaterial = temp.oldMaterial;
        this.oldBlockData = temp.oldBlockData;
        return this;
    }
}
