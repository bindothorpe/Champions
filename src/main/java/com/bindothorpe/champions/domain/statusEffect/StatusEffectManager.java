package com.bindothorpe.champions.domain.statusEffect;

import com.bindothorpe.champions.DomainController;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatusEffectManager {

    private static StatusEffectManager instance;

    private final DomainController dc;

    private final Map<StatusEffectType, StatusEffect> statusEffectMap = new HashMap<>();

    private StatusEffectManager(DomainController dc) {
        this.dc = dc;
    }

    public static StatusEffectManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new StatusEffectManager(dc);
        }
        return instance;
    }

    public void registerStatusEffect(StatusEffect statusEffect) {
        statusEffectMap.put(statusEffect.getType(), statusEffect);
        dc.getPlugin().getServer().getPluginManager().registerEvents(statusEffect, dc.getPlugin());
    }

    public void addStatusEffectToEntity(StatusEffectType type, UUID uuid, double duration) {
        StatusEffect effect = statusEffectMap.get(type);
        if(effect == null)
            throw new IllegalArgumentException("Status effect " + type + " has not been registered yet");
        statusEffectMap.get(type).addEntity(uuid, duration);

        if(duration != -1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    removeStatusEffectFromPlayer(type, uuid);
                }
            }.runTaskLater(dc.getPlugin(), (long) (duration * 20));
        }
    }

    public void removeStatusEffectFromPlayer(StatusEffectType type, UUID uuid) {
        StatusEffect effect = statusEffectMap.get(type);
        if(effect == null)
            throw new IllegalArgumentException("Status effect " + type + " has not been registered yet");
        statusEffectMap.get(type).removeEntity(uuid);
    }
}
