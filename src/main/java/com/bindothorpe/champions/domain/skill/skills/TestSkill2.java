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

public class TestSkill2 extends Skill {

    private List<Integer> damage = Arrays.asList(3, 5, 8, 12);

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
        description.add(Component.text("This is a test skill").color(NamedTextColor.GRAY));
        description.add(Component.text("Right-click").color(NamedTextColor.YELLOW)
                .append(Component.text(" to deal ").color(NamedTextColor.GRAY))
                .append(Component.text(damage.get(Math.max(skillLevel - 1, 0))).color(NamedTextColor.RED))
                .append(Component.text(" damage").color(NamedTextColor.RED)));

        return description;
    }
}
