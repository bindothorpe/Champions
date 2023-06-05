package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.build.BuildManager;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.player.PlayerManager;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillManager;

import java.util.*;

public class DomainController {

    private final ChampionsPlugin plugin;
    private final SkillManager skillManager = SkillManager.getInstance(this);
    private final PlayerManager playerManager = PlayerManager.getInstance(this);
    private final BuildManager buildManager = BuildManager.getInstance(this);

    public DomainController(ChampionsPlugin plugin) {
        this.plugin = plugin;
    }

    public ChampionsPlugin getPlugin() {
        return plugin;
    }

    public void setSelectedBuildIdForPlayer(UUID uuid, String buildId) {
        playerManager.setSelectedBuildIdForPlayer(uuid, buildId);
    }

    public String getSelectedBuildIdFromPlayer(UUID uuid) {
        return playerManager.getSelectedBuildIdFromPlayer(uuid);
    }

    public Map<ClassType, Set<String>> getBuildIdsFromPlayer(UUID uuid) {
        return playerManager.getBuildIdsFromPlayer(uuid);
    }

    public boolean addBuildIdToPlayer(UUID uuid, ClassType classType, String buildId) {
        return playerManager.addBuildIdToPlayer(uuid, classType, buildId);
    }

    public boolean removeBuildIdFromPlayer(UUID uuid, String buildId) {
        return playerManager.removeBuildIdFromPlayer(uuid, buildId);
    }

    public void equipSkillForUser(UUID uuid, SkillId skillId, int level) {
        skillManager.equipSkillForUser(uuid, skillId, level);
    }

    public void unequipSkillForPlayer(UUID uuid, SkillId skillId) {
        skillManager.unequipSkillForPlayer(uuid, skillId);
    }

    public String getSkillName(SkillId skillId) {
        return skillManager.getSkillName(skillId);
    }

    public List<String> getSkillDescription(SkillId skillId) {
        return skillManager.getSkillDescription(skillId);
    }

    public List<Double> getSkillCooldownDuration(SkillId skillId) {
        return skillManager.getSkillCooldownDuration(skillId);
    }

    public int getSkillMaxLevel(SkillId skillId) {
        return skillManager.getSkillMaxLevel(skillId);
    }

    public int getSkillLevelUpCost(SkillId skillId) {
        return skillManager.getSkillLevelUpCost(skillId);
    }

    public void registerSkill(Skill skill) {
        skillManager.registerSkill(skill);
    }

    public Map<ClassType, Set<Build>> getBuilds(Collection<String> buildsIds) {
        return buildManager.getBuilds(buildsIds);
    }

    public Build getBuild(String buildId) {
        return buildManager.getBuild(buildId);
    }

    public String createEmptyBuild(ClassType classType) {
        return buildManager.createEmptyBuild(classType);
    }

    public boolean deleteBuild(String buildId) {
        return buildManager.deleteBuild(buildId);
    }

    public boolean levelUpSkillForBuild(String buildId, SkillId skillId) {
        return buildManager.levelUpSkillForBuild(buildId, skillId);
    }

    public boolean levelDownSkillForBuild(String buildId, SkillId skillId) {
        return buildManager.levelDownSkillForBuild(buildId, skillId);
    }

    public void equipBuildForPlayer(UUID uuid, String buildId) {
        buildManager.equipBuildForPlayer(uuid, buildId);
    }

    public void unequipBuildForPlayer(UUID uuid) {
        buildManager.unequipBuildForPlayer(uuid);
    }

}
