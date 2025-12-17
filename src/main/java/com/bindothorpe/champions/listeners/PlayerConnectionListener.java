package com.bindothorpe.champions.listeners;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.util.belowName.BelowNameUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    private final DomainController dc;

    public PlayerConnectionListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BelowNameUtil.cleanup(event.getPlayer());
    }

}
