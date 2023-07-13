package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.entity.Player;

public class SkillChargeEndEvent extends SkillChargeEvent{
    public SkillChargeEndEvent(Player player, SkillId skillId, int skillLevel, int charge) {
        super(player, skillId, skillLevel, charge);
    }
}
