package com.bindothorpe.champions.domain.game;

import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.game.GameScoreChangeEvent;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class GameScore {

    private final Map<TeamColor, Integer> scores = new HashMap<>();

    public GameScore() {
        for(TeamColor team : TeamColor.values()) {
            scores.put(team, 0);
        }
    }

    public void addScore(TeamColor team, int score) {
        if(score < 0)
            throw new IllegalArgumentException("Score must be positive");
        scores.put(team, Math.min(1000, scores.get(team) + score));
        Bukkit.getPluginManager().callEvent(new GameScoreChangeEvent(team, scores.get(team)));
    }

    public int getScore(TeamColor team) {
        return scores.get(team);
    }
}
