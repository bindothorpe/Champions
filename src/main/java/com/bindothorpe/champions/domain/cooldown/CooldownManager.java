package com.bindothorpe.champions.domain.cooldown;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.cooldown.CooldownEndEvent;
import com.bindothorpe.champions.events.cooldown.CooldownStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private static CooldownManager instance;

    private final DomainController dc;

    private static Map<UUID, Map<Object, Long>> cooldowns = new HashMap<>();
    private static Map<UUID, Map<Object, BukkitTask>> cooldownTasks = new HashMap<>();

    private CooldownManager(DomainController dc) {
        this.dc = dc;
    }

    public static CooldownManager getInstance(DomainController dc) {
        if (instance == null)
            instance = new CooldownManager(dc);
        return instance;
    }

    public void startCooldown(UUID uuid, Object source, double durationInSeconds) {
        if (!cooldowns.containsKey(uuid))
            cooldowns.put(uuid, new HashMap<>());

        cooldowns.get(uuid).put(source, System.currentTimeMillis() + (long) (durationInSeconds * 1000));

        if (!cooldownTasks.containsKey(uuid))
            cooldownTasks.put(uuid, new HashMap<>());

        cooldownTasks.get(uuid).put(source, new BukkitRunnable() {
                    @Override
                    public void run() {
                        cooldowns.get(uuid).remove(source);
                        cooldownTasks.get(uuid).remove(source);
                        this.cancel();
                        onCooldownEnd(uuid, source);
                    }
                }.runTaskLater(dc.getPlugin(), (long) (durationInSeconds * 20))
        );

        Bukkit.getPluginManager().callEvent(new CooldownStartEvent(uuid, source));
    }

    public double getCooldownRemaining(UUID uuid, Object source) {
        if (!cooldowns.containsKey(uuid))
            return 0;

        if (!cooldowns.get(uuid).containsKey(source))
            return 0;

        return (cooldowns.get(uuid).get(source) - System.currentTimeMillis()) / 1000.0;
    }

    public boolean isOnCooldown(UUID uuid, Object source) {
        return getCooldownRemaining(uuid, source) > 0;
    }

    public void reduceCooldown(UUID uuid, Object source, double durationInSeconds) {
        if (!cooldowns.containsKey(uuid))
            return;

        if (!cooldowns.get(uuid).containsKey(source))
            return;

        long newCooldown = cooldowns.get(uuid).get(source) - (long) (durationInSeconds * 1000);
        cooldowns.get(uuid).put(source, newCooldown);

        // Cancel the existing task.
        if(cooldownTasks.get(uuid).containsKey(source)) {
            cooldownTasks.get(uuid).get(source).cancel();
            cooldownTasks.get(uuid).remove(source);
        }

        // If the new cooldown has already passed, call onCooldownEnd directly.
        if (newCooldown <= System.currentTimeMillis()) {
            cooldowns.get(uuid).remove(source);
            onCooldownEnd(uuid, source);
        } else {
            // Schedule a new task with the reduced duration.
            cooldownTasks.get(uuid).put(source, new BukkitRunnable() {
                @Override
                public void run() {
                    cooldowns.get(uuid).remove(source);
                    cooldownTasks.get(uuid).remove(source);
                    this.cancel();
                    onCooldownEnd(uuid, source);
                }
            }.runTaskLater(dc.getPlugin(), (newCooldown - System.currentTimeMillis()) / 50));
        }
    }



    private void onCooldownEnd(UUID uuid, Object source) {
        Bukkit.getPluginManager().callEvent(new CooldownEndEvent(uuid, source));
    }

}
