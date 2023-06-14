package com.bindothorpe.champions.domain.player;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {

    private final DomainController dc;
    private final UUID uuid;
    private final Map<ClassType, Set<String>> buildIds;
    private String selectedBuildId;

    public PlayerData(DomainController dc, UUID uuid, Map<ClassType, Set<String>> buildIds) {
        this.dc = dc;
        this.uuid = uuid;
        // In the future this data would come from a MySQL database
        // For now we just pass an empty Map
        if(buildIds == null) {
            buildIds = new HashMap<>();
            for(ClassType type : ClassType.values()) {
                buildIds.put(type, new HashSet<>());
            }
        }
        this.buildIds = buildIds;
    }


    public String getSelectedBuildId() {
        return selectedBuildId;
    }

    public void setSelectedBuildId(String buildId) {
        if(buildId == null) {
            selectedBuildId = null;
            return;
        }

        for(Set<String> builds : buildIds.values()) {
            if(builds.contains(buildId)) {
                selectedBuildId = buildId;
                return;
            }
        }

    }

    public Map<ClassType, Set<String>> getBuildIds() {
        return buildIds;
    }

    public boolean addBuildId(ClassType classType, String buildId) {
        if(buildIds.get(classType).size() == getMaxBuilds())
            return false;

        buildIds.get(classType).add(buildId);
        return true;
    }

    public boolean removeBuildId(String buildId) {
        for(Map.Entry<ClassType, Set<String>> entry: buildIds.entrySet()) {
            if(entry.getValue().contains(buildId)) {
                buildIds.get(entry.getKey()).remove(buildId);
                return true;
            }
        }

        return false;
    }

    public int getBuildCountByClassType(ClassType classType) {
        return buildIds.get(classType).size();
    }

    public int getMaxBuilds() {
        return 7;
    }
}
