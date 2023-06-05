package com.bindothorpe.champions.domain;

import com.bindothorpe.champions.ChampionsPlugin;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillManager {

    private static SkillManager instance;

    private final Map<SkillId, Skill> skills;

    private SkillManager() {
        skills = new HashMap<>();
    }

    public void registerSkill(Skill skill, ChampionsPlugin plugin) {
        if(skill == null)
            return;
        skills.put(skill.getId(), skill);
        Bukkit.getPluginManager().registerEvents(skill, plugin);
    }

    public Skill getSkill(SkillId skillId) {
        return skills.get(skillId);
    }

    public static SkillManager getInstance() {
        if(instance == null)
            instance = new SkillManager();

        return instance;
    }



}
