package com.bindothorpe.champions.events.interact;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerBlockListener implements Listener {

    private static final long BLOCK_GRACE_PERIOD_IN_MILLIS = 300L;
    private static final Map<UUID, Long> blockingStartTimestampMap = new HashMap<>();

    private final DomainController dc;

    public PlayerBlockListener(DomainController dc) {
        this.dc = dc;
        dc.getPlugin().getLogger().info("Registered player block listener");
    }

    public static double getPlayerBlockingDuration(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if(!blockingStartTimestampMap.containsKey(uuid)) return 0.0;

        long startTime = blockingStartTimestampMap.get(uuid);
        long endTime = System.currentTimeMillis();
        long blockDurationInMilliseconds = endTime - startTime;
        return (double) blockDurationInMilliseconds / 1000;
    }


    @EventHandler
    public void onPlayerRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        player.sendMessage("0");

        if(!isHoldingShield(player)) return;

        player.sendMessage("1");

        blockingStartTimestampMap.computeIfAbsent(uuid, k -> System.currentTimeMillis());

        PlayerStartBlockingEvent startBlockingEvent = new PlayerStartBlockingEvent(player);
        startBlockingEvent.callEvent();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        for(UUID uuid : blockingStartTimestampMap.keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;;

            if(player.isBlocking() || isUserInGracePeriod(uuid)) {

            } else {
                if (!blockingStartTimestampMap.containsKey(uuid)) continue;
                if(isUserInGracePeriod(uuid)) continue;

                long startTime = blockingStartTimestampMap.remove(uuid);
                long endTime = System.currentTimeMillis();

                long blockDurationInMilliseconds = endTime - startTime;
                double blockDuration = (double) blockDurationInMilliseconds / 1000;

                PlayerStopBlockingEvent stopBlockingEvent = new PlayerStopBlockingEvent(player, blockDuration, blockDurationInMilliseconds);
                stopBlockingEvent.callEvent();

            }
        }
    }

    private static boolean isUserInGracePeriod(UUID uuid) {
        if(!blockingStartTimestampMap.containsKey(uuid)) return false;
        return System.currentTimeMillis() - blockingStartTimestampMap.get(uuid) < BLOCK_GRACE_PERIOD_IN_MILLIS;
    }

    private boolean isHoldingShield(Player player) {
        if(player == null) return false;
        return player.getInventory().getItemInOffHand().getType().equals(Material.SHIELD);
    }

    public static boolean isPlayerBlocking(@NotNull Player player) {
        return player.isBlocking() || isUserInGracePeriod(player.getUniqueId());
    }
}
