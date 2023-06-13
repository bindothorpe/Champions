package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ComponentUtil;
import jdk.jfr.EventType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HuntersHeart extends Skill {

    private final List<Double> healing = Arrays.asList(1.0, 2.0, 4.0);
    private static final double DURATION = 5.0;
    public HuntersHeart(DomainController dc) {
        super(dc, SkillId.HUNTERS_HEART, SkillType.PASSIVE_B, ClassType.RANGER, "Hunter's Heart", null, 3, 1);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TWO_SECOND))
            return;

        for (UUID uuid : getUsers()) {
            if(dc.hasTakenDamageWithinDuration(uuid, DURATION))
                continue;

            Player player = Bukkit.getPlayer(uuid);

            if(player.getHealth() == player.getMaxHealth())
                continue;

            player.setHealth(Math.min(player.getHealth() + healing.get(getSkillLevel(uuid) - 1), player.getMaxHealth()));

        }

    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                        .append(Component.text("Heal for").color(NamedTextColor.GRAY)));
        lore.add(ComponentUtil.skillLevelValues(skillLevel, healing, NamedTextColor.YELLOW)
                .append(Component.text(" health every").color(NamedTextColor.GRAY)));
        lore.add(Component.text("2.0 seconds, after not taking").color(NamedTextColor.GRAY));
        lore.add(Component.text("damage for ").color(NamedTextColor.GRAY)
                .append(Component.text(DURATION).color(NamedTextColor.GRAY))
                .append(Component.text(" seconds").color(NamedTextColor.GRAY)));
        return lore;
    }
}
