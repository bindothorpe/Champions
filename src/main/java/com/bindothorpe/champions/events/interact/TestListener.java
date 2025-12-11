package com.bindothorpe.champions.events.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TestListener implements Listener {

    @EventHandler
    public void onPlayerStartBlock(PlayerStartBlockingEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("You started blocking.");
    }

    @EventHandler
    public void onPlayerStopBlock(PlayerStopBlockingEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("You stopped blocking.");
        player.sendMessage(String.format("You blocked for %.1f seconds (%d millis)",event.getBlockDuration(), event.getBlockDurationInMilliseconds()));
    }
}
