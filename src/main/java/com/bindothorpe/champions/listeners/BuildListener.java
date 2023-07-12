package com.bindothorpe.champions.listeners;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.database.DatabaseController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.game.GameState;
import com.bindothorpe.champions.events.build.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
                    dc.getPlayerManager().addBuildIdToPlayer(event.getPlayer().getUniqueId(), build.getClassType(), build.getId());
                    dc.getBuildManager().addBuild(build);
                }
                dbc.getPlayerSelectedBuildByUUID(event.getPlayer().getUniqueId(), new DatabaseResponse<String>() {
                    @Override
                    public void onResult(String result) {
                        dc.getPlayerManager().setSelectedBuildIdForPlayer(event.getPlayer().getUniqueId(), result);
                        if (result != null)
                            dc.getBuildManager().equipBuildForPlayer(event.getPlayer().getUniqueId(), result);
                    }
                });
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if(event.getClickedInventory() == null)
            return;


        if(!(event.getClickedInventory().getType().equals(InventoryType.PLAYER) && isBlacklist(event.getSlot())))
            return;

        if(dc.getGameManager().getGameState().equals(GameState.LOBBY) || dc.getGameManager().getGameState().equals(GameState.LOBBY_COUNTDOWN))
            return;
        event.setCancelled(true);
    }

    private boolean isBlacklist(int slot) {
        //If the slot is between 0 and 8 or 36 and 40 return true
        return (slot >= 0 && slot <= 8) || (slot >= 36 && slot <= 40);
    }


    //TODO: Im not sure why there is no eventhandler here
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for(Set<String> ids : dc.getPlayerManager().getBuildIdsFromPlayer(event.getPlayer().getUniqueId()).values()) {
            for(String id : ids) {
                dc.getBuildManager().deleteBuild(id);
            }
        }
        dc.getPlayerManager().deletePlayer(event.getPlayer().getUniqueId());
    }
}
