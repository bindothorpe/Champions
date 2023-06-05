package com.bindothorpe.champions.domain.build;

import com.bindothorpe.champions.domain.skill.SkillId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Build {

    private static final int MAX_SKILL_POINTS = 15;

    private String id;
    private ClassType classType;
    private Map<SkillId, Integer> skills;

    private int skillPoints;

    public Build(String id, ClassType classType, Map<SkillId, Integer> skills, int skillPoints) {
        this.id = id;
        this.classType = classType;
        this.skills = skills;
        this.skillPoints = skillPoints;
    }

    public Build(ClassType classType) {
        this(UUID.randomUUID().toString(),
                classType,
                new HashMap<>(),
                MAX_SKILL_POINTS);
    }

    public String getId() {
        return id;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Map<SkillId, Integer> getSkills() {
        return skills;
    }

    public boolean levelUpSkill(SkillId skillId, int maxLevel, int cost) {
        if(!skills.containsKey(skillId)) {
            return false;
        }

        if(skillPoints < cost) {
            return false;
        }

        if(skills.get(skillId) == maxLevel) {
            return false;
        }

        skills.put(skillId, skills.get(skillId) + 1);
        skillPoints -= cost;
        return true;
    }

    public boolean levelDownSkill(SkillId skillId, int cost) {
        if(!skills.containsKey(skillId)) {
            return false;
        }

        if(skills.get(skillId) == 0) {
            return false;
        }

        skills.put(skillId, skills.get(skillId) - 1);
        skillPoints += cost;
        return true;
    }
}
