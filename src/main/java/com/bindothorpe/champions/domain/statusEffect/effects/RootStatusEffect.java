package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RootStatusEffect extends StatusEffect {

    private static final Map<UUID, Vector> locationMap = new HashMap<>();

    public RootStatusEffect(DomainController dc) {
        super(dc, "Root", StatusEffectType.ROOT);
    }

    @Override
    public void addEntity(UUID uuid, double duration) {
        super.addEntity(uuid, duration);
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null)
            throw new IllegalArgumentException("Entity with UUID " + uuid + " does not exist");
        locationMap.put(uuid, entity.getLocation().toVector());
    }

    @Override
    public void removeEntity(UUID uuid) {
        super.removeEntity(uuid);
        locationMap.remove(uuid);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for (UUID uuid : locationMap.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null)
                continue;



            Location location = entity.getLocation();
            Vector vector = locationMap.get(uuid);

            if (location.getX() != vector.getX() || location.getY() != vector.getY() || location.getZ() != vector.getZ()) {
                location.setX(vector.getX());
                location.setY(vector.getY());
                location.setZ(vector.getZ());
                entity.teleport(location);
            }

        }
    }
}
