package com.bindothorpe.champions.listeners.game.capturepoint;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.game.capturepoint.CapturePointCaptureEvent;
import com.bindothorpe.champions.events.game.capturepoint.CapturePointStartCaptureEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CapturePointListener implements Listener {

    private final DomainController dc;
    private static Map<UUID, Long> lastStartCaptureMessage = new HashMap<>();

    public CapturePointListener(DomainController dc) {
        this.dc = dc;
    }

    @EventHandler
    public void onCapturePointStartCapture(CapturePointStartCaptureEvent event) {
        TeamColor team = event.getTeam();

        if(team == null)
            return;

        long lastStartCaptureMessageTime = lastStartCaptureMessage.getOrDefault(event.getCapturePoint().getId(), 0L);

        // if less than 5 seconds have passed since the last message, don't send another one
        if(System.currentTimeMillis() - lastStartCaptureMessageTime < 5000)
            return;

        for(Player player : Bukkit.getOnlinePlayers()) {
            TeamColor playerTeam = dc.getTeamFromEntity(player);
            lastStartCaptureMessage.put(event.getCapturePoint().getId(), System.currentTimeMillis());

            if(playerTeam == null)
                continue;

            if(playerTeam.equals(team))
                continue;

            ChatUtil.sendGameMessage(player,
                    Component.text(TextUtil.camelCasing(event.getCapturePoint().getName())).color(NamedTextColor.YELLOW)
                    .append(Component.text(" is being captured by team ").color(NamedTextColor.GRAY))
                    .append(Component.text(TextUtil.camelCasing(team.name())).color(team.getTextColor())));


        }
    }

    @EventHandler
    public void onCapturePointCaptured(CapturePointCaptureEvent event) {
        TeamColor team = event.getTeam();

        ChatUtil.sendGameBroadcast(
                Component.text(TextUtil.camelCasing(event.getCapturePoint().getName())).color(NamedTextColor.YELLOW)
                .append(Component.text(" has been captured by team ").color(NamedTextColor.GRAY))
                .append(Component.text(TextUtil.camelCasing(team.name())).color(team.getTextColor())));
    }

}
