package com.bindothorpe.champions.listeners.game;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.GameManager;
import com.bindothorpe.champions.domain.game.GameState;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameListener implements Listener {

    private final DomainController dc;

    public GameListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {

        if(!event.getUpdateType().equals(UpdateType.SECOND))
            return;

        GameManager gm = dc.getGameManager();

        if(!gm.getGameState().equals(GameState.IN_PROGRESS))
            return;

        for(TeamColor team : TeamColor.values()) {
            int capturePoints = gm.getCapturePointManager().getCapturePointsCapturedByTeam(team);

            if(capturePoints == 0)
                continue;

            gm.getGameScore().addScore(team, capturePoints * 10);
        }
    }
}
