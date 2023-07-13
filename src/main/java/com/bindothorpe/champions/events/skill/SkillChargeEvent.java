package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.entity.Player;

public class SkillChargeEvent extends SkillEvent {
    private final int charge;

    public SkillChargeEvent(Player player, SkillId skillId, int skillLevel, int charge) {
        super(player, skillId, skillLevel);
        this.charge = charge;
    }

    public int getCharge() {
        return charge;
    }
}
