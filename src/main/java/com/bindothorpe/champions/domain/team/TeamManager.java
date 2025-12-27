package com.bindothorpe.champions.domain.team;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

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

    public boolean areEntitiesOnDifferentTeams(Entity entity1, Entity entity2) {
        TeamColor team1 = getTeamFromEntity(entity1);
        TeamColor team2 = getTeamFromEntity(entity2);

        return team1 != team2;
    }

    public void addEntityToTeam(Entity entity, TeamColor teamColor) {
        addEntityToTeam(entity, teamColor, false);
    }


    public void addEntityToTeam(Entity entity, TeamColor teamColor, boolean verbose) {
        Team team = teams.get(getTeamFromEntity(entity));
        if(team != null) {
            team.removeEntry(entity.getUniqueId().toString());
        }
        team = teams.get(teamColor);
        team.addEntry(entity.getUniqueId().toString());

        if(entity instanceof Player player) {
            player.playerListName(Component.text(player.getName()).color(teamColor.getTextColor()));
            dc.getScoreboardManager().updateScoreboard(entity.getUniqueId());
            if(verbose) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.GAME,
                        Component.text("You are now on the ").color(NamedTextColor.GRAY)
                                .append(Component.text(teamColor.toString()).color(teamColor.getTextColor()))
                                .append(Component.text(" team.").color(NamedTextColor.GRAY)));
            }
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

    public List<Player> getPlayersOnTeamOfEntity(Entity entity) {
        return getPlayersOnTeam(getTeamFromEntity(entity));
    }

        public List<Player> getPlayersOnTeam(TeamColor color) {
        List<Player> players = new ArrayList<>();
        if(color == null) return players;
        if(!teams.containsKey(color)) return players;

        teams.get(color).getEntries().forEach((uuidString) -> {
            UUID uuid = UUID.fromString(uuidString);
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) return;
            players.add(player);
        });

        return players;
    }
}
