package com.bindothorpe.champions.events.game.capturepoint;

import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import com.bindothorpe.champions.domain.team.TeamColor;

public class CapturePointStartCaptureEvent extends CapturePointEvent{

    private final TeamColor team;
    public CapturePointStartCaptureEvent(CapturePoint capturePoint, TeamColor team) {
        super(capturePoint);
        this.team = team;
    }

    public TeamColor getTeam() {
        return team;
    }
}
