package com.bindothorpe.champions.events.build;

import com.bindothorpe.champions.domain.build.Build;

import java.util.UUID;

public class UnequipBuildEvent extends BuildEvent{

    private final UUID playerId;

    public UnequipBuildEvent(Build build, UUID playerId) {
        super(build);
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}
