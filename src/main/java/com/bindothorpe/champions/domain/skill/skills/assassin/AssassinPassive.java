package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import net.kyori.adventure.text.Component;

import java.util.*;

public class AssassinPassive extends Skill implements ReloadableData {

    private static double MOVE_SPEED_MOD;

    public AssassinPassive(DomainController dc) {
        super(dc,"Assassin Passive", SkillId.ASSASSIN_PASSIVE, SkillType.CLASS_PASSIVE, ClassType.ASSASSIN);
    }

    @Override
    public void addUser(UUID uuid, int skillLevel) {
        super.addUser(uuid, skillLevel);
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(EntityStatusType.MOVEMENT_SPEED, MOVE_SPEED_MOD, -1.0, false, false, this));
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

    @Override
    public void onReload() {
        try {
            MOVE_SPEED_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.passive.move_speed_mod");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}
