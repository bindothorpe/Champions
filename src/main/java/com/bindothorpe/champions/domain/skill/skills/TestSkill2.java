package com.bindothorpe.champions.domain.skill.skills;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSkill2 extends Skill {

    private List<Integer> damage = Arrays.asList(3, 5, 8, 12);
    private List<Double> passive = Arrays.asList(0.8, 1.2, 1.4, 1.6);

    public TestSkill2() {
        super(SkillId.TEST_SKILL_2, SkillType.SWORD, ClassType.GLOBAL, "Test Skill 2", new ArrayList<>(), Arrays.asList(8.5, 7.0, 4.5, 3.5), 4, 2);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.getHand().equals(EquipmentSlot.HAND))
            return;

        player.sendMessage("You right clicked");

        if (!isUser(player.getUniqueId()))
            addUser(player.getUniqueId(), 2);

        activate(player.getUniqueId());
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> description = new ArrayList<>();
        description.add(Component.text("Active: ").color(NamedTextColor.WHITE)
                .append(Component.text("right-click").color(NamedTextColor.YELLOW))
                .append(Component.text(" to deal").color(NamedTextColor.GRAY)));
        description.add(ComponentUtil.skillLevelValues(skillLevel, damage, NamedTextColor.RED)
                .append(Component.text(" damage").color(NamedTextColor.RED)));
        description.add(Component.text(" "));
        description.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("increases ").color(NamedTextColor.GRAY))
                .append(Component.text("attack damage").color(NamedTextColor.GRAY)));
        description.add(Component.text("by ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, passive, NamedTextColor.RED))
                .append(Component.text(" %").color(NamedTextColor.RED)));

        return description;
    }
}
