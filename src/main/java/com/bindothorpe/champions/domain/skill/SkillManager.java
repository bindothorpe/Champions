package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.player.PlayerData;
import com.bindothorpe.champions.domain.player.PlayerManager;
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
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        skillMap.get(skillId).addUser(uuid, level);
    }

    public void unequipSkillForPlayer(UUID uuid, SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        skillMap.get(skillId).removeUser(uuid);
    }

    public String getSkillName(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        return skillMap.get(skillId).getName();
    }

    public List<String> getSkillDescription(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        return skillMap.get(skillId).getDescription();
    }

    public List<Double> getSkillCooldownDuration(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        return skillMap.get(skillId).getCooldownDuration();
    }
    public int getSkillMaxLevel(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        return skillMap.get(skillId).getMaxLevel();
    }
    public int getSkillLevelUpCost(SkillId skillId) {
        if(!skillMap.containsKey(skillId))
            throw new IllegalArgumentException(String.format("Skill with id \"%s\" has not been registered. Please make sure that this skill is registered."));
        return skillMap.get(skillId).getLevelUpCost();
    }
//    + registerSkill(skill : Skill) : void
    public void registerSkill(Skill skill) {
        Bukkit.getPluginManager().registerEvents(skill, dc.getPlugin());
        skillMap.put(skill.getId(), skill);
    }


    public List<SkillType> getSkillTypes() {
        return Arrays.asList(SkillType.SWORD, SkillType.AXE, SkillType.BOW, SkillType.PASSIVE_A, SkillType.PASSIVE_B, SkillType.PASSIVE_C);
    }
}
