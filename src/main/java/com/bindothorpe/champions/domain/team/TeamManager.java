package com.bindothorpe.champions.domain.team;

import com.bindothorpe.champions.DomainController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class TeamManager {

    private static TeamManager instance;
    private final DomainController dc;

    private Map<TeamColor, Team> teams;

    private Scoreboard scoreboard;

    private TeamManager(DomainController dc) {
        this.dc = dc;
        initialize();
    }

    public static TeamManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new TeamManager(dc);
        }
        return instance;
    }

    public void addEntityToTeam(Entity entity, TeamColor teamColor) {
        Team team = teams.get(getTeamFromEntity(entity));
        if(team != null) {
            team.removeEntry(entity.getUniqueId().toString());
        }
        team = teams.get(teamColor);
        team.addEntry(entity.getUniqueId().toString());

        if(entity instanceof Player) {
            Player player = (Player) entity;
            player.playerListName(Component.text(player.getName()).color(teamColor.getTextColor()));
        }

        if(entity instanceof Player) {
            dc.getScoreboardManager().updateScoreboard(entity.getUniqueId());
        }
    }

    public void removeEntityFromTeam(Entity entity) {
        Team team = teams.get(getTeamFromEntity(entity));
        if(team != null) {
            team.removeEntry(entity.getUniqueId().toString());
        }

        if(entity instanceof Player) {
            Player player = (Player) entity;
            player.playerListName(Component.text(player.getName()).color(NamedTextColor.WHITE));
        }
    }

    public TeamColor getTeamFromEntity(Entity entity) {
        for(Map.Entry<TeamColor, Team> entry : teams.entrySet()) {

            if(entry.getValue().hasEntry(entity.getUniqueId().toString())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void initialize() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if(scoreboard.getObjective("champions") == null) {
            Objective objective = scoreboard.registerNewObjective("champions", "dummy", Component.text("Champions").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        teams = new HashMap<>();

        for(TeamColor color : TeamColor.values()) {
            if(scoreboard.getTeam(color.name()) == null) {
                Team team = scoreboard.registerNewTeam(color.name());
                team.color(color.getTextColor());
                team.displayName(Component.text(color.name()).color(color.getTextColor()));
                teams.put(color, team);
            } else {
                teams.put(color, scoreboard.getTeam(color.name()));
            }
        }
    }
}
