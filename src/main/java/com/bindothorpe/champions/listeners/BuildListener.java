package com.bindothorpe.champions.listeners;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.database.DatabaseController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.events.build.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Set;

public class BuildListener implements Listener {

    private final DomainController dc;
    private final DatabaseController dbc;

    public BuildListener(DomainController dc) {
        this.dc = dc;
        this.dbc = dc.getDatabaseController();
    }

    @EventHandler
    public void onCreateBuild(CreateBuildEvent event) {
        dbc.createBuild(event.getBuild(), event.getPlayerId());
    }

    @EventHandler
    public void onUpdateBuild(UpdateBuildEvent event) {
        dbc.updateBuild(event.getBuild());
    }

    @EventHandler
    public void onDeleteBuild(DeleteBuildEvent event) {
        dbc.deleteBuild(event.getBuild().getId());
    }

    @EventHandler
    public void onEquipBuild(EquipBuildEvent event) {
        dbc.createPlayerSelectedBuild(event.getPlayerId(), event.getBuild().getId());
    }

    @EventHandler
    public void onUnequipBuild(UnequipBuildEvent event) {
        dbc.updatePlayerSelectedBuild(event.getPlayerId(), null);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        dbc.getBuildsByPlayerUUID(event.getPlayer().getUniqueId(), new DatabaseResponse<List<Build>>() {
            @Override
            public void onResult(List<Build> result) {
                for (Build build : result) {
                    dc.addBuildIdToPlayer(event.getPlayer().getUniqueId(), build.getClassType(), build.getId());
                    dc.addBuild(build);
                }
                dbc.getPlayerSelectedBuildByUUID(event.getPlayer().getUniqueId(), new DatabaseResponse<String>() {
                    @Override
                    public void onResult(String result) {
                        dc.setSelectedBuildIdForPlayer(event.getPlayer().getUniqueId(), result);
                        if (result != null)
                            dc.equipBuildForPlayer(event.getPlayer().getUniqueId(), result);
                    }
                });
            }
        });


    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        for(Set<String> ids : dc.getBuildIdsFromPlayer(event.getPlayer().getUniqueId()).values()) {
            for(String id : ids) {
                dc.deleteBuild(id);
            }
        }
        dc.deletePlayer(event.getPlayer().getUniqueId());
    }
}
