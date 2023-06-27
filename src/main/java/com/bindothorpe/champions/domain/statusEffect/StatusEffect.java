package com.bindothorpe.champions.domain.statusEffect;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class StatusEffect implements Listener {

    protected final DomainController dc;
    private final String name;
    private final StatusEffectType type;
    private final Map<UUID, Long> activeEntities;
    private final Map<UUID, BukkitTask> taskMap;

    public StatusEffect(DomainController dc, String name, StatusEffectType type) {
        this.dc = dc;
        this.name = name;
        this.type = type;
        activeEntities = new HashMap<>();
        taskMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public boolean isActive(UUID uuid) {
        return activeEntities.containsKey(uuid);
    }

    public double getTimeLeft(UUID uuid) {
        if (!isActive(uuid))
            return -1;
        return activeEntities.get(uuid) - System.currentTimeMillis();
    }

    public void addEntity(UUID uuid, double duration) {
        activeEntities.put(uuid, System.currentTimeMillis() + ((long) duration * 1000L));
        taskMap.put(uuid, new BukkitRunnable() {
            @Override
            public void run() {
                if (getTimeLeft(uuid) <= 0) {
                    taskMap.remove(uuid);
                    removeEntity(uuid);
                }
            }
        }.runTaskLater(dc.getPlugin(), (long) (duration * 20L)));
    }

    public void removeEntity(UUID uuid) {
        activeEntities.remove(uuid);
        if(taskMap.containsKey(uuid)) {
            taskMap.get(uuid).cancel();
            taskMap.remove(uuid);
        }

    }

    public StatusEffectType getType() {
        return type;
    }

}
