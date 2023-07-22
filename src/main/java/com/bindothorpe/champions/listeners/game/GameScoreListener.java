package com.bindothorpe.champions.listeners.game;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.GameState;
import com.bindothorpe.champions.events.game.GameScoreChangeEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameScoreListener implements Listener {

    private final DomainController dc;

    public GameScoreListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onGameScoreChange(GameScoreChangeEvent event) {
        dc.getScoreboardManager().updateScoreboard();

        if(event.getScore() >= 1000) {
            dc.getGameManager().setGameState(GameState.GAME_END_COUNTDOWN);
            ChatUtil.sendBroadcast(ChatUtil.Prefix.GAME,
                    Component.text(TextUtil.camelCasing(event.getTeam().toString())).color(event.getTeam().getTextColor())
                    .append(Component.text(" has won the game!").color(NamedTextColor.GRAY)));
        }
    }
}
