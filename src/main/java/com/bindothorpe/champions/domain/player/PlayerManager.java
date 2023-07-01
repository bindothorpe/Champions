package com.bindothorpe.champions.domain.player;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {

    private static PlayerManager instance;
    private final DomainController dc;
    private final Map<UUID, PlayerData> playerDataMap;

    private PlayerManager(DomainController dc) {
        this.dc = dc;
        // In the future, this would load all data from the MySQL Database
        // For now we just create a new Object
        playerDataMap = new HashMap<>();
    }

    public static PlayerManager getInstance(DomainController dc) {
        if(instance == null)
            instance = new PlayerManager(dc);
        return instance;
    }

    public void setSelectedBuildIdForPlayer(UUID uuid, String buildId) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        if(buildId == null) {
            playerDataMap.get(uuid).setSelectedBuildId(null);
            return;
        }

        playerDataMap.get(uuid).setSelectedBuildId(buildId);
    }

    public String getSelectedBuildIdFromPlayer(UUID uuid) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        return playerDataMap.get(uuid).getSelectedBuildId();
    }

    public Map<ClassType, Set<String>> getBuildIdsFromPlayer(UUID uuid) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        return playerDataMap.get(uuid).getBuildIds();
    }

    public boolean addBuildIdToPlayer(UUID uuid, ClassType classType, String buildId) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        return playerDataMap.get(uuid).addBuildId(classType, buildId);
    }

    public boolean removeBuildIdFromPlayer(UUID uuid, String buildId) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        boolean success = playerDataMap.get(uuid).removeBuildId(buildId);
        if(success) {
            dc.unequipBuildForPlayer(uuid);
        }
        return success;
    }

    private void createPlayerDataForPlayer(UUID uuid) {
        playerDataMap.put(uuid, new PlayerData(dc, uuid, null));
    }

    public int getBuildCountByClassTypeForPlayer(ClassType classType, UUID uuid) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        return playerDataMap.get(uuid).getBuildCountByClassType(classType);
    }

    public int getMaxBuildsForPlayer(UUID uuid) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        return playerDataMap.get(uuid).getMaxBuilds();
    }

    public void deletePlayer(UUID uuid) {
        if(!playerDataMap.containsKey(uuid))
            return;

        playerDataMap.remove(uuid);
    }

    public int getGold(UUID uuid) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        return playerDataMap.get(uuid).getGold();
    }

    public void addGold(UUID uuid, int gold) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        playerDataMap.get(uuid).addGold(gold);
    }

    public void reduceGold(UUID uuid, int gold) {
        if(!playerDataMap.containsKey(uuid))
            createPlayerDataForPlayer(uuid);

        playerDataMap.get(uuid).reduceGold(gold);
    }
}
