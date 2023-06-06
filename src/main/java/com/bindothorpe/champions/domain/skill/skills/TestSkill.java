package com.bindothorpe.champions.domain.skill.skills;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;

public class TestSkill extends Skill {
    public TestSkill() {
        super(SkillId.TEST_SKILL, SkillType.SWORD, ClassType.ASSASSIN, "Test Skill", new ArrayList<>(), Arrays.asList(5.0, 3.0, 2.0), 3, 1);
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
}
