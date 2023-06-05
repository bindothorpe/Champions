package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.ClassType;
import com.bindothorpe.champions.domain.Skill;
import com.bindothorpe.champions.domain.SkillId;
import com.bindothorpe.champions.domain.SkillManager;
import com.bindothorpe.champions.domain.player.PlayerManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DomainController {

    private final ChampionsPlugin plugin;
//    private final SkillManager skillManager = SkillManager.getInstance();
    private final PlayerManager playerManager = PlayerManager.getInstance(this);

    public DomainController(ChampionsPlugin plugin) {
        this.plugin = plugin;
    }

//    public void registerSkill(Skill skill) {
//        skillManager.registerSkill(skill, plugin);
//    }
//
//    public Skill getSkill(SkillId skillId) {
//        return skillManager.getSkill(skillId);
//    }

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

}
