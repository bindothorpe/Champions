package com.bindothorpe.champions.domain.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;

import java.util.*;

public class BuildManager {

    private DomainController dc;
    private static BuildManager instance;
    private Map<String, Build> buildMap;

    private BuildManager(DomainController dc) {
        this.dc = dc;
        //TODO: Load data from database
        this.buildMap = new HashMap<>();
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
        SkillType skillType = dc.getSkillType(skillId);

        return build.levelUpSkill(skillType, skillId, maxLevel, cost);
    }

    public boolean levelDownSkillForBuild(String buildId, SkillId skillId) {
        Build build = buildMap.get(buildId);

        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        int cost = dc.getSkillLevelUpCost(skillId);
        SkillType skillType = dc.getSkillType(skillId);

        return build.levelDownSkill(skillType, skillId, cost);
    }

    public void equipBuildForPlayer(UUID uuid, String buildId) {
        unequipBuildForPlayer(uuid);

        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        Map<SkillType, Integer> skillLevels = build.getSkillLevels();
        for (Map.Entry<SkillType, SkillId> entry : build.getSkills().entrySet()) {
            if(entry.getValue() == null) {
                continue;
            }

            dc.equipSkillForUser(uuid, entry.getValue(), skillLevels.get(entry.getKey()));
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

        for (SkillId skillId : build.getSkills().values()) {
            dc.unequipSkillForPlayer(uuid, skillId);
        }

        dc.setSelectedBuildIdForPlayer(uuid, null);
    }

    public SkillId getSkillFromBuild(String buildId, SkillType skillType) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }
        return build.getSkill(skillType);
    }
    public int getSkillLevelFromBuild(String buildId, SkillType skillType) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }
        return build.getSkillLevel(skillType);
    }

    public ClassType getClassTypeFromBuild(String buildId) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        return buildMap.get(buildId).getClassType();
    }

    public int getSkillPointsFromBuild(String buildId) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        return build.getSkillPoints();
    }
}
