package com.bindothorpe.champions.commands;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Deprecated
public class SkillsCommand implements CommandExecutor {

    private DomainController dc;

    public SkillsCommand(DomainController dc) {
        this.dc = dc;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You need to be a player to perform this command.");
            return true;
        }

        Player player = (Player) commandSender;

        String buildId = dc.getPlayerManager().getSelectedBuildIdFromPlayer(player.getUniqueId());

        if(buildId == null)
            return true;

        Arrays.stream(SkillType.values()).forEach(skillType -> {
            if(skillType.equals(SkillType.CLASS_PASSIVE)) {
                return;
            }
            player.sendMessage(Component.text(TextUtil.camelCasing(skillType.toString())).color(NamedTextColor.GRAY)
                    .append(Component.text(": "))
                    .append(Component.text(dc.getBuildManager().getSkillLevelFromBuild(buildId, skillType)).color(NamedTextColor.YELLOW)));
        });

        return true;
    }
}
