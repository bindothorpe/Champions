package com.bindothorpe.champions.domain.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;

import java.util.*;

public class BuildManager {

    private DomainController dc;
    private static BuildManager instance;
    private Map<String, Build> buildMap;

    private BuildManager(DomainController dc) {
        this.dc = dc;
    }

    public static BuildManager getInstance(DomainController dc) {
        if (instance == null)
            instance = new BuildManager(dc);

        return instance;
    }

    public Map<ClassType, Set<Build>> getBuilds(Collection<String> buildsIds) {
        Map<ClassType, Set<Build>> builds = new HashMap<>();
        for (ClassType type : ClassType.values()) {
            builds.put(type, new HashSet<>());
        }

        for (String buildId : buildsIds) {
            Build build = buildMap.get(buildId);
            if (build == null) {
                throw new RuntimeException(String.format("Build with id \"%s\" not found.", buildId));
            }

            builds.get(build.getClassType()).add(build);
        }

        return builds;
    }

    public Build getBuild(String buildId) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }
        return build;
    }

    public String createEmptyBuild(ClassType classType) {
        Build build = new Build(classType);
        String buildId = build.getId();

        buildMap.put(buildId, build);
        return buildId;
    }

    public boolean deleteBuild(String buildId) {
        return buildMap.remove(buildId) != null;
    }

    public boolean levelUpSkillForBuild(String buildId, SkillId skillId) {
        Build build = buildMap.get(buildId);

        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        int maxLevel = dc.getSkillMaxLevel(skillId);
        int cost = dc.getSkillLevelUpCost(skillId);

        return build.levelUpSkill(skillId, maxLevel, cost);
    }

    public boolean levelDownSkillForBuild(String buildId, SkillId skillId) {
        Build build = buildMap.get(buildId);

        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        int cost = dc.getSkillLevelUpCost(skillId);

        return build.levelDownSkill(skillId, cost);
    }

    public void equipBuildForPlayer(UUID uuid, String buildId) {
        unequipBuildForPlayer(uuid);

        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        for (Map.Entry<SkillId, Integer> entry : build.getSkills().entrySet()) {
            dc.equipSkillForUser(uuid, entry.getKey(), entry.getValue());
        }

        dc.setSelectedBuildIdForPlayer(uuid, buildId);
    }

    public void unequipBuildForPlayer(UUID uuid) {
        String buildId = dc.getSelectedBuildIdFromPlayer(uuid);
        if (buildId == null) {
            return;
        }

        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        for (SkillId skillId : build.getSkills().keySet()) {
            dc.unequipSkillForPlayer(uuid, skillId);
        }

        dc.setSelectedBuildIdForPlayer(uuid, null);
    }
}
