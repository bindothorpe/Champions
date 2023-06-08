package com.bindothorpe.champions.events.update;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.util.TextUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Updater {

    private static Updater instance;
    private BukkitTask task;
    private static final Map<UpdateType, Long> lastCalled = new HashMap<>();
    private DomainController dc;

    public Updater(DomainController dc) {
        this.dc = dc;
        Arrays.stream(UpdateType.values()).forEach(updateType -> lastCalled.put(updateType, System.currentTimeMillis()));
    }

    public static Updater getInstance(DomainController dc) {
        if (instance == null) {
            instance = new Updater(dc);
        }
        return instance;
    }

    public void start() {
        if(task != null) {
            return;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {

                for(UpdateType updateType : UpdateType.values()) {
                    if(System.currentTimeMillis() - lastCalled.get(updateType) >= updateType.getTime()) {
                        lastCalled.put(updateType, System.currentTimeMillis());
                        dc.getPlugin().getServer().getPluginManager().callEvent(new UpdateEvent(updateType));
                    }
                }
            }
        }.runTaskTimer(dc.getPlugin(), 0, 1);
    }

    public void stop() {
        if(task == null) {
            return;
        }
        task.cancel();
        task = null;
    }

}
