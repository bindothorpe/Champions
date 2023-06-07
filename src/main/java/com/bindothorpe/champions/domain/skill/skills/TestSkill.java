package com.bindothorpe.champions.domain.skill.skills;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSkill extends Skill {

    private List<Integer> healing = Arrays.asList(3, 5, 8);

    public TestSkill() {
        super(SkillId.TEST_SKILL, SkillType.AXE, ClassType.MAGE, "Test Skill", new ArrayList<>(), Arrays.asList(5.0, 3.0, 2.0), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(!event.getHand().equals(EquipmentSlot.HAND))
            return;

        player.sendMessage("You right clicked");

        if(!isUser(player.getUniqueId()))
            addUser(player.getUniqueId(), 2);

        activate(player.getUniqueId());
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        description.add(Component.text("This is a test skill").color(NamedTextColor.GRAY));
        description.add(Component.text("Right-click").color(NamedTextColor.YELLOW)
                .append(Component.text(" to heal ").color(NamedTextColor.GRAY))
                .append(Component.text(healing.get(Math.max(skillLevel - 1, 0))).color(NamedTextColor.GREEN))
                .append(Component.text(" hp").color(NamedTextColor.GREEN)));

        return description;
    }
}
