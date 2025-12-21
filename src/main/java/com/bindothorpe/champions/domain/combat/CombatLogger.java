package com.bindothorpe.champions.domain.combat;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLogger {

    private static CombatLogger instance;
    private static final double COMBAT_TIMEOUT = 10.0;
    private final DomainController dc;

    private final Map<UUID, Long> lastDamageTaken = new HashMap<>();
    private final Map<UUID, Long> lastDamageDealt = new HashMap<>();
    private final Map<UUID, DamageLog> lastDamageTakenLog = new HashMap<>();

    private CombatLogger(DomainController dc) {
        this.dc = dc;
    }

    public static CombatLogger getInstance(DomainController dc) {
        if (instance == null) {
            instance = new CombatLogger(dc);
        }
        return instance;
    }

    public void logDamage(UUID damageTaker, @Nullable UUID damageDealer, CustomDamageEvent.DamageCause damageCause, @Nullable String damageSourceString) {
        lastDamageTakenLog.put(damageTaker, new DamageLog(damageCause, damageTaker, damageDealer, System.currentTimeMillis(), damageSourceString));
    }

    public void logDamageTaken(UUID uuid) {
        lastDamageTaken.put(uuid, System.currentTimeMillis());
    }

    public void logDamageDealt(UUID uuid) {
        lastDamageDealt.put(uuid, System.currentTimeMillis());
    }

    public boolean isInCombat(UUID uuid, double duration) {
        return hasTakenDamageWithinDuration(uuid, duration) || hasDealtDamageWithinDuration(uuid, duration);
    }

    public boolean isInCombat(UUID uuid) {
        return isInCombat(uuid, COMBAT_TIMEOUT);
    }

    public boolean hasTakenDamageWithinDuration(UUID uuid, double duration) {
        return lastDamageTaken.containsKey(uuid) && System.currentTimeMillis() - lastDamageTaken.get(uuid) < duration * 1000;
    }

    public boolean hasDealtDamageWithinDuration(UUID uuid, double duration) {
        return lastDamageDealt.containsKey(uuid) && System.currentTimeMillis() - lastDamageDealt.get(uuid) < duration * 1000;
    }

    public DamageLog getLastLog(@NotNull UUID uniqueId) {
        return lastDamageTakenLog.get(uniqueId);
    }
}
