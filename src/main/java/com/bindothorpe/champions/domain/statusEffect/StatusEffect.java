package com.bindothorpe.champions.domain.statusEffect;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.NamespacedKey;
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
    private final Map<UUID, StatusEffectData> activeEntitiesDataMap = new HashMap<>();

    public StatusEffect(DomainController dc, String name, StatusEffectType type) {
        this.dc = dc;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isActive(UUID uuid) {
        activeEntitiesDataMap.computeIfAbsent(uuid, k -> new StatusEffectData(dc, uuid, this::handleEntityValueChanged));
        return activeEntitiesDataMap.get(uuid).hasAtLeastOnceInstance();
    }

    public int getHighestAmplifier(UUID uuid) {
        activeEntitiesDataMap.computeIfAbsent(uuid, k -> new StatusEffectData(dc, uuid, this::handleEntityValueChanged));
        return activeEntitiesDataMap.get(uuid).getHighestAmplifier();
    }

    public void addEntityWithSource(UUID uuid, NamespacedKey sourceKey, int amplifier, double duration) {
        activeEntitiesDataMap.computeIfAbsent(uuid, k -> new StatusEffectData(dc, uuid, this::handleEntityValueChanged));
        activeEntitiesDataMap.get(uuid).addEffectInstance(sourceKey, amplifier, duration);
        handleEntityValueChanged(uuid);
    }

    public void removeEntityWithSource(UUID uuid, NamespacedKey sourceKey) {
        activeEntitiesDataMap.computeIfAbsent(uuid, k -> new StatusEffectData(dc, uuid, this::handleEntityValueChanged));
        activeEntitiesDataMap.get(uuid).removeEffectInstance(sourceKey);
        handleEntityValueChanged(uuid);
    }

    public abstract void handleEntityValueChanged(UUID uuid);

    public StatusEffectType getType() {
        return type;
    }

}
