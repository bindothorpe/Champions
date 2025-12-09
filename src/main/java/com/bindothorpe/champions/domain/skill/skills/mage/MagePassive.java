package com.bindothorpe.champions.domain.skill.skills.mage;

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

public class MagePassive extends Skill implements ReloadableData {

    private static double COOLDOWN_REDUCTION;

    public MagePassive(DomainController dc) {
        super(dc, "Mage Passive", SkillId.MAGE_PASSIVE, SkillType.CLASS_PASSIVE, ClassType.MAGE);
    }

    @Override
    public void addUser(UUID uuid, int skillLevel) {
        super.addUser(uuid, skillLevel);
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(EntityStatusType.COOLDOWN_REDUCTION, 0.1, -1.0, true, false, this));
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



    @Override
    public void onReload() {
        try {
            COOLDOWN_REDUCTION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.passive.cooldown_reduction");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}
