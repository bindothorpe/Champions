package com.bindothorpe.champions.domain.build;

import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Build {

    private static final int MAX_SKILL_POINTS = 15;

    private String id;
    private ClassType classType;
    private Map<SkillType, SkillId> skills;
    private Map<SkillType, Integer> skillLevels;

    private int skillPoints;

    public Build(String id, ClassType classType, Map<SkillType, SkillId> skills, Map<SkillType, Integer> skillLevels, int skillPoints) {
        this.id = id;
        this.classType = classType;
        this.skills = skills;
        this.skillLevels = skillLevels;
        this.skillPoints = skillPoints;
    }

    public Build(ClassType classType) {
        this.id = UUID.randomUUID().toString();
        this.classType = classType;
        this.skills = new HashMap<>();
        skillLevels = new HashMap<>();

        Arrays.stream(SkillType.values()).filter(s -> !s.equals(SkillType.CLASS_PASSIVE)).forEach(s -> {
            skills.put(s, null);
            skillLevels.put(s, 0);
        });

        //TODO: add class passive
        skills.put(SkillType.CLASS_PASSIVE, null);
        skillLevels.put(SkillType.CLASS_PASSIVE, 0);

        this.skillPoints = MAX_SKILL_POINTS;
    }

    public String getId() {
        return id;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Map<SkillType, SkillId> getSkills() {
        return skills;
    }

    public SkillId getSkill(SkillType skillType) {
        return skills.get(skillType);
    }

    public Map<SkillType, Integer> getSkillLevels() {
        return skillLevels;
    }
    public int getSkillLevel(SkillType skillType) {
        return skillLevels.get(skillType);
    }

    public boolean levelUpSkill(SkillType skillType, SkillId skillId, int maxLevel, int cost) {

        // Check if there are enough skill points left
        if(skillPoints < cost) {
            return false;
        }

        // Add skill and level it up, if the skill type has no asigned skillid
        if(skills.get(skillType) == null) {
            skills.put(skillType, skillId);
            skillLevels.put(skillType, 1);
            skillPoints -= cost;
            return true;
        }

        // Check if the skill the player is trying to level up, is in fact the skill that is equiped
        if (!skills.get(skillType).equals(skillId)) {
            return false;
        }

        // Check if the skill is already max level
        if(skillLevels.get(skillId) == maxLevel) {
            return false;
        }

        // Level up the skill and reduce the remaining skill points
        skillLevels.put(skillType, skillLevels.get(skillId) + 1);
        skillPoints -= cost;
        return true;
    }

    public boolean levelDownSkill(SkillType skillType, SkillId skillId, int cost) {
        //TODO: Implement

        // Check if the skill you want to level down is the skill that is equiped
        if(!skills.get(skillType).equals(skillId)) {
            return false;
        }

        // Check if the skill is not already level 0
        if(skillLevels.get(skillType) == 0) {
            return false;
        }



        skillLevels.put(skillType, skillLevels.get(skillId) - 1);
        skillPoints += cost;

        // If the skill level is equal to 0, remove the skill from the skills list
        if(skillLevels.get(skillType) == 0) {
            skills.put(skillType, null);
        }

        return true;
    }
}
