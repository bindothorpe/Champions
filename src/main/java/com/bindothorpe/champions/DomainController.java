package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.Skill;
import com.bindothorpe.champions.domain.SkillId;
import com.bindothorpe.champions.domain.SkillManager;

public class DomainController {

    private final SkillManager skillManager = SkillManager.getInstance();
    private final ChampionsPlugin plugin;

    public DomainController(ChampionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerSkill(Skill skill) {
        skillManager.registerSkill(skill, plugin);
    }

    public Skill getSkill(SkillId skillId) {
        return skillManager.getSkill(skillId);
    }

}
