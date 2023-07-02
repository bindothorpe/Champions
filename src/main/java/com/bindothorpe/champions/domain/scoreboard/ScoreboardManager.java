package com.bindothorpe.champions.domain.scoreboard;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.util.TextUtil;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {

    private static ScoreboardManager instance;

    private final DomainController dc;

    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private ScoreboardManager(DomainController dc) {
        this.dc = dc;
    }

    public static ScoreboardManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new ScoreboardManager(dc);
        }
        return instance;
    }

    public void setScoreboard(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if(player == null)
            return;

        FastBoard board = new FastBoard(player);
        board.updateTitle(Component.text("Champions").color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
        boards.put(uuid, board);

        updateScoreboard(uuid);
    }

    public void removeScoreboard(UUID uuid) {
        FastBoard board = this.boards.remove(uuid);

        if (board != null) {
            board.delete();
        }

    }

    public void updateScoreboard(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if(player == null)
            return;

        FastBoard board = boards.get(uuid);

        if(board == null)
            return;

        TeamColor playerTeam = dc.getTeamFromEntity(player);

        List<Component> lines = new ArrayList<>();

        lines.add(Component.empty());
        if(playerTeam != null) {
            String teamName = TextUtil.camelCasing(playerTeam.name());
            lines.add(Component.text("Team: ").color(NamedTextColor.WHITE));
            lines.add(Component.text(teamName).color(playerTeam.getTextColor()));
            lines.add(Component.empty());
        }

        lines.add(Component.text("KDA: ").color(NamedTextColor.WHITE));
        lines.add(Component.text(dc.getKills(uuid)).color(NamedTextColor.YELLOW)
                .append(Component.text("/").color(NamedTextColor.WHITE))
                .append(Component.text(dc.getDeaths(uuid))).color(NamedTextColor.YELLOW)
                .append(Component.text("/").color(NamedTextColor.WHITE))
                .append(Component.text(dc.getAssists(uuid))).color(NamedTextColor.YELLOW));

        lines.add(Component.empty());

        lines.add(Component.text("Gold: ").color(NamedTextColor.WHITE));
        String formattedNumber = String.format(Locale.ENGLISH, "%,d", dc.getGold(uuid));
        lines.add(Component.text(formattedNumber).color(NamedTextColor.YELLOW));
        board.updateLines(lines);

    }
}
