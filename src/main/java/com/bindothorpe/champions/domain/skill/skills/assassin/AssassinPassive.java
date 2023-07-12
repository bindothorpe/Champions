package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import net.kyori.adventure.text.Component;

import java.util.*;

public class AssassinPassive extends Skill {

    private final Map<UUID, Set<UUID>> effects;

    public AssassinPassive(DomainController dc) {
        super(dc, SkillId.ASSASSIN_PASSIVE, SkillType.CLASS_PASSIVE, ClassType.ASSASSIN, "Assassin Passive", null, 1, 0);
        this.effects = new HashMap<>();
    }

    @Override
    public void addUser(UUID uuid, int skillLevel) {
        super.addUser(uuid, skillLevel);
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(EntityStatusType.MOVEMENT_SPEED, 0.2, -1.0, false, false, this));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(EntityStatusType.KNOCKBACK_DONE, 0.0, -1.0, true, true, this));
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);
    }

    @Override
    public void removeUser(UUID uuid) {
        super.removeUser(uuid);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED, this);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.KNOCKBACK_DONE, this);
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return null;
    }
}
