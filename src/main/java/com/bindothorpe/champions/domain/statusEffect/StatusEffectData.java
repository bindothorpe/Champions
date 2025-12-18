package com.bindothorpe.champions.domain.statusEffect;

import com.bindothorpe.champions.DomainController;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class StatusEffectData {

    private final DomainController dc;

    public static final int INFINITE = -1;

    private final UUID ownerUUID;
    private final Map<NamespacedKey, Integer> amplifierMap = new HashMap<>();
    private final Map<NamespacedKey, Long> endTimestampMap = new HashMap<>();
    private final Map<NamespacedKey, BukkitTask> endOfEffectTask = new HashMap<>();
    private final Consumer<UUID> onTimerEnd;


    public StatusEffectData(DomainController dc, UUID ownerUUID, Consumer<UUID> onTimerEnd) {
        this.dc = dc;
        this.ownerUUID = ownerUUID;
        this.onTimerEnd = onTimerEnd;
    }

    public void addEffectInstance(NamespacedKey sourceKey, int amplifier, double duration) {
        if(endTimestampMap.containsKey(sourceKey)) {
            removeEffectInstance(sourceKey);
        }

        amplifierMap.put(sourceKey, amplifier);

        if(duration == INFINITE) {
            endTimestampMap.put(sourceKey, System.currentTimeMillis() + ((long) duration * 1000L));
            return;
        }

        endTimestampMap.put(sourceKey, (long) INFINITE);

        endOfEffectTask.put(sourceKey, new BukkitRunnable() {
            @Override
            public void run() {
                amplifierMap.remove(sourceKey);
                endTimestampMap.remove(sourceKey);
                endOfEffectTask.remove(sourceKey);
                onTimerEnd.accept(ownerUUID);
            }
        }.runTaskLater(dc.getPlugin(), (long) (duration * 20L)));
    }


    public void addEffectInstance(NamespacedKey sourceKey, int amplifier) {
        addEffectInstance(sourceKey, amplifier, -1);
    }

    public void removeEffectInstance(NamespacedKey sourceKey) {
        amplifierMap.remove(sourceKey);
        endTimestampMap.remove(sourceKey);
        BukkitTask task = endOfEffectTask.remove(sourceKey);

        if(task != null) task.cancel();


    }

    /**
     * Returns true if there is at least once instance active.
     * @return boolean
     */
    public boolean hasAtLeastOnceInstance() {
        return !endTimestampMap.isEmpty();
    }

    /**
     * Returns the highest amplifier value. Returns 0 if none are present
     * @return int
     */
    public int getHighestAmplifier() {
        return amplifierMap.isEmpty() ? 0 : java.util.Collections.max(amplifierMap.values());
    }
}
