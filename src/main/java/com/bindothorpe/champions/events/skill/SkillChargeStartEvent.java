package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.entity.Player;

public class SkillChargeStartEvent extends SkillChargeEvent{
    public SkillChargeStartEvent(Player player, SkillId skillId, int skillLevel, int charge) {
        super(player, skillId, skillLevel, charge);
    }
}
