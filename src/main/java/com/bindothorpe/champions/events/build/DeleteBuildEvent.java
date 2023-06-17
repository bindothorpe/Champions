package com.bindothorpe.champions.events.build;

import com.bindothorpe.champions.domain.build.Build;

import java.util.UUID;

public class DeleteBuildEvent extends BuildEvent {

    private final UUID playerId;

    public DeleteBuildEvent(Build build, UUID playerId) {
        super(build);
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}
