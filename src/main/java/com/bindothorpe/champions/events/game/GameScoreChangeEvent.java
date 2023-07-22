package com.bindothorpe.champions.events.game;

import com.bindothorpe.champions.domain.team.TeamColor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameScoreChangeEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final TeamColor teamColor;
    private final int score;

    public GameScoreChangeEvent(TeamColor teamColor, int score) {
        this.teamColor = teamColor;
        this.score = score;
    }

    public TeamColor getTeam() {
        return teamColor;
    }

    public int getScore() {
        return score;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
