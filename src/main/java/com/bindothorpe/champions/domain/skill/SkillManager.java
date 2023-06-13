package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.*;

public class SkillManager {

    private static SkillManager instance;
    private final DomainController dc;

    private Map<SkillId, Skill> skillMap;

    private SkillManager(DomainController dc) {
        this.dc = dc;
        // In the future, this would load all data from the MySQL Database
        // For now we just create a new Object
        skillMap = new HashMap<>();
    }

    public static SkillManager getInstance(DomainController dc) {
        if(instance == null)
            instance = new SkillManager(dc);
        return instance;
    }

    public void equipSkillForUser(UUID uuid, SkillId skillId, int level) {
        if(skillId == null)
            return;
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        skillMap.get(skillId).addUser(uuid, level);
    }

    public void unequipSkillForPlayer(UUID uuid, SkillId skillId) {
        if(skillId == null)
            return;
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        skillMap.get(skillId).removeUser(uuid);
    }

    public String getSkillName(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        return skillMap.get(skillId).getName();
    }

    public List<Component> getSkillDescription(SkillId skillId, int skillLevel) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        return skillMap.get(skillId).getDescription(skillLevel);
    }

    public List<Double> getSkillCooldownDuration(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        return skillMap.get(skillId).getCooldownDuration();
    }
    public int getSkillMaxLevel(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        return skillMap.get(skillId).getMaxLevel();
    }
    public int getSkillLevelUpCost(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered.", skillId));
        return skillMap.get(skillId).getLevelUpCost();
    }
//    + registerSkill(skill : Skill) : void
    public void registerSkill(Skill skill) {
        Bukkit.getPluginManager().registerEvents(skill, dc.getPlugin());
        skillMap.put(skill.getId(), skill);
    }


    public SkillType getSkillType(SkillId skillId) {
        return skillMap.get(skillId).getSkillType();
    }

    public Set<SkillId> getClassSkillsForSkillType(ClassType classType, SkillType skillType) {
        Set<SkillId> skillIds = new HashSet<>();
        for(Skill skill : skillMap.values()) {
            if((skill.getClassType() == classType || skill.getClassType() == ClassType.GLOBAL) && skill.getSkillType() == skillType)
                skillIds.add(skill.getId());
        }
        return skillIds;
    }

}
