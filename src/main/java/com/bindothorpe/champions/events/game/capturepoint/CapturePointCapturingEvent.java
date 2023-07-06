package com.bindothorpe.champions.events.game.capturepoint;

import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import com.bindothorpe.champions.domain.team.TeamColor;

public class CapturePointCapturingEvent extends CapturePointEvent{
    private final TeamColor capturingTeam;

    public CapturePointCapturingEvent(CapturePoint capturePoint, TeamColor capturingTeam) {
        super(capturePoint);
        this.capturingTeam = capturingTeam;
    }

    public TeamColor getCapturingTeam() {
        return capturingTeam;
    }
}
