package com.bindothorpe.champions.domain.skill.skills.global;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.AttemptResult;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.util.belowName.BelowNameUtil;
import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestSkill extends Skill {

    private final Set<UUID> active = new HashSet<>();


    public TestSkill(DomainController dc) {
        super(dc, "Test Skill", SkillId.TEST_SKILL, SkillType.SWORD, ClassType.GLOBAL);
        MAX_LEVEL = 1;
        LEVEL_UP_COST = 1;
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        Player player = event.getPlayer();

        if(active.contains(player.getUniqueId())) {
            BelowNameUtil.clear(player);
        } else {
            BelowNameUtil.display(player, Component.text("You are gay", NamedTextColor.YELLOW));
        }
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerRightClickEvent rightClickEvent)) return AttemptResult.FALSE;

        if(rightClickEvent.isSword()) return AttemptResult.FALSE;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
