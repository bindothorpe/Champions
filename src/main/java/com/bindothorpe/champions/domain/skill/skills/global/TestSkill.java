package com.bindothorpe.champions.domain.skill.skills.global;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestSkill extends Skill {
    public TestSkill(DomainController dc) {
        super(dc, SkillId.TEST_SKILL, SkillType.SWORD, ClassType.GLOBAL, "Stun your self", Arrays.asList(3.0), 1, 2);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        boolean success = activate(event.getPlayer().getUniqueId(), event);

        if (!success) {
            return;
        }

        dc.addStatusEffectToEntity(StatusEffectType.STUN, event.getPlayer().getUniqueId(), 1.5D);
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerRightClickEvent))
            return false;

        PlayerRightClickEvent e = (PlayerRightClickEvent) event;

        if(!e.isSword())
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();

        lore.add(ComponentUtil.active()
                .append(ComponentUtil.rightClick())
                .append(Component.text("to stun your self")));

        return lore;
    }
}
