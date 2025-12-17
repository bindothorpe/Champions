package com.bindothorpe.champions.util.belowName;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class BelowNameUtil {

    private static final String OBJECTIVE_NAME = "belowName";

    private BelowNameUtil() {}

    /**
     * Clears the below name display for a specific player (removes it for all viewers)
     * @param player The player whose below name should be cleared
     */
    public static void clear(Player player) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = mainScoreboard.getObjective(OBJECTIVE_NAME);

        if (objective != null) {
            mainScoreboard.resetScores(player.getName());
        }
    }

    /**
     * Removes the below name team for a player, typically called when they go offline
     * This prevents memory leaks from accumulating teams for offline players
     * @param player The player whose team should be removed
     */
    public static void cleanup(Player player) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "bn_" + player.getName();
        org.bukkit.scoreboard.Team team = mainScoreboard.getTeam(teamName);

        if (team != null) {
            team.unregister();
        }

        // Also clear their score if objective exists
        Objective objective = mainScoreboard.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            mainScoreboard.resetScores(player.getName());
        }
    }


    /**
     * Displays a message below a specific player's name (visible to all players)
     * @param player The player whose name should have text below it
     * @param message The message to display below the player's name
     */
    public static void display(Player player, String message) {
        display(player, Component.text(message));
    }

    /**
     * Displays a message below a specific player's name (visible to all players)
     * @param player The player whose name should have text below it
     * @param message The message to display below the player's name
     */
    public static void display(Player player, Component message) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Get or create the below name objective
        Objective objective = mainScoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = mainScoreboard.registerNewObjective(
                    OBJECTIVE_NAME,
                    Criteria.DUMMY,
                    Component.empty()
            );
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        // Create a team for this specific player if it doesn't exist
        String teamName = "bn_" + player.getName();
        org.bukkit.scoreboard.Team team = mainScoreboard.getTeam(teamName);

        if (team == null) {
            team = mainScoreboard.registerNewTeam(teamName);
        }

        // Remove player from any existing below-name team
        for (org.bukkit.scoreboard.Team t : mainScoreboard.getTeams()) {
            if (t.getName().startsWith("bn_") && t.hasEntry(player.getName())) {
                t.removeEntry(player.getName());
            }
        }

        // Add player to their specific team and set the suffix
        team.addEntry(player.getName());
        team.suffix(message);

        // Set score so it displays
        objective.getScore(player.getName()).setScore(0);
    }
}