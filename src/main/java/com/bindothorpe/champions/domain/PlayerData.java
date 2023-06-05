package com.bindothorpe.champions.domain;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private List<String> builds;

    private String selectedBuildId;

    public PlayerData(UUID uuid, List<String> builds) {
        this.uuid = uuid;
        this.builds = builds;
    }

    public void setSelectedBuildId(String buildId) {
        if(!builds.contains(buildId))
            return;
        selectedBuildId = buildId;
    }




}
