package com.bindothorpe.champions.domain.skills;

import com.bindothorpe.champions.domain.ClassType;
import com.bindothorpe.champions.domain.Skill;
import com.bindothorpe.champions.domain.SkillId;
import com.bindothorpe.champions.domain.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;

public class TestSkill extends Skill {
    public TestSkill() {
        super(SkillId.TEST_SKILL, SkillType.SWORD, ClassType.ASSASSIN, "Test Skill", new ArrayList<>(), 3);
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
