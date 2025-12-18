package com.bindothorpe.champions.listeners.projectile;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.timer.Timer;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArrowListener implements Listener {

    private static final double ARROW_LIFETIME = 3.0D;

    private final DomainController dc;
    private final Map<UUID, Timer> arrowTimerMap = new HashMap<>();

    public ArrowListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onArrowHitBlock(ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof Arrow arrow)) return;

        if(!(arrow.getShooter() instanceof Player player)) return;

        if(event.getHitBlock() == null) return;

        if(!dc.getPlayerManager().hasBuildSelected(player.getUniqueId())) return;

        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        startTimer(arrow);
    }

    @EventHandler
    public void onArrowRemove(EntityRemoveEvent event) {
        if(!(event.getEntity() instanceof Arrow arrow)) return;

        if(!arrowTimerMap.containsKey(arrow.getUniqueId())) return;

        clearTimer(arrow);
    }



    private void startTimer(@NotNull Arrow arrow) {
        UUID uuid = arrow.getUniqueId();

        Timer timer = new Timer(dc.getPlugin(), ARROW_LIFETIME, () -> {
            clearTimer(arrow);
            arrowTimerMap.remove(uuid);
            arrow.remove();
        });

        arrowTimerMap.put(uuid, timer);
        timer.start();
    }

    private void clearTimer(@NotNull Arrow arrow) {
        UUID uuid = arrow.getUniqueId();

        if(!arrowTimerMap.containsKey(uuid)) return;

        Timer timer = arrowTimerMap.get(uuid);
        timer.stop();
        arrowTimerMap.remove(uuid);
    }
}
