package com.bindothorpe.champions.domain.game.capturePoint;

import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class CapturePointManager implements Listener {

    private static CapturePointManager instance;

    private final Map<String, CapturePoint> capturePointMap = new HashMap<>();

    private CapturePointManager() {}

    public static CapturePointManager getInstance() {
        if(instance == null) {
            instance = new CapturePointManager();
        }
        return instance;
    }

    public boolean addCapturePoint(CapturePoint capturePoint) {
        if(capturePointMap.containsKey(capturePoint.getName()))
            return false;

        capturePointMap.put(capturePoint.getName(), capturePoint);
        return true;
    }

    public void removeCapturePoint(String capturePointName) {
        if(!capturePointMap.containsKey(capturePointName))
            return;

        capturePointMap.remove(capturePointName);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.HALF_SECOND))
            return;

        capturePointMap.values().forEach(CapturePoint::update);
    }

    public int getCapturePointsCapturedByTeam(TeamColor teamColor) {
        return (int) capturePointMap.values().stream().filter(capturePoint -> teamColor.equals(capturePoint.getTeam())).count();
    }
}
