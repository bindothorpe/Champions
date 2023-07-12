package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import net.kyori.adventure.text.Component;

import java.util.*;

public class MagePassive extends Skill {

    private final Map<UUID, Set<UUID>> effects;

    public MagePassive(DomainController dc) {
        super(dc, SkillId.MAGE_PASSIVE, SkillType.CLASS_PASSIVE, ClassType.MAGE, "Mage Passive", null, 1, 0);
        this.effects = new HashMap<>();
    }

    @Override
    public void addUser(UUID uuid, int skillLevel) {
        super.addUser(uuid, skillLevel);
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(EntityStatusType.COOLDOWN_REDUCTION, 0.5, -1.0, true, false, this));
    }

    @Override
    public void removeUser(UUID uuid) {
        super.removeUser(uuid);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.COOLDOWN_REDUCTION, this);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return null;
    }
}
