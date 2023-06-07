package com.bindothorpe.champions.domain.skill.skills;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestSkill extends Skill {

    private final List<Double> healing = Arrays.asList(3.0, 5.0, 8.5);
    private final List<Double> passiveHealing = Arrays.asList(0.5, 1.0, 1.5);


    public TestSkill() {
        super(SkillId.TEST_SKILL, SkillType.AXE, ClassType.MAGE, "Test Skill", new ArrayList<>(), Arrays.asList(5.0, 3.0, 2.0), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.getHand().equals(EquipmentSlot.HAND))
            return;

        boolean success = activate(player.getUniqueId());

        if (!success) {
            return;
        }

        player.setHealth(player.getHealth() + healing.get(Math.max(getSkillLevel(player.getUniqueId()) - 1, 0)));
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> description = new ArrayList<>();

        description.add(Component.text("Active: ").color(NamedTextColor.WHITE)
                .append(Component.text("right-click").color(NamedTextColor.YELLOW))
                .append(Component.text(" to heal").color(NamedTextColor.GRAY)));
        description.add(ComponentUtil.skillLevelValues(skillLevel, healing, NamedTextColor.GREEN)
                .append(Component.text(" hp").color(NamedTextColor.GREEN)));
        description.add(Component.text(" "));
        description.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("after not getting").color(NamedTextColor.GRAY)));
        description.add(Component.text("hit for 5 seconds, heal").color(NamedTextColor.GRAY));
        description.add(ComponentUtil.skillLevelValues(skillLevel, passiveHealing, NamedTextColor.GREEN)
                .append(Component.text(" hp").color(NamedTextColor.GREEN))
                .append(Component.text(" every").color(NamedTextColor.GRAY)));
        description.add(Component.text("2 seconds").color(NamedTextColor.GRAY));


        return description;
    }
}
