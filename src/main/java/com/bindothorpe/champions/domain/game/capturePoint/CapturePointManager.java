package com.bindothorpe.champions.domain.game.capturePoint;

import com.bindothorpe.champions.domain.game.GameManager;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class CapturePointManager implements Listener {

    private static CapturePointManager instance;

    private final GameManager gameManager;

    private final Map<String, CapturePoint> capturePointMap = new HashMap<>();

    private CapturePointManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public static CapturePointManager getInstance(GameManager gameManager) {
        if(instance == null) {
            instance = new CapturePointManager(gameManager);
        }
        return instance;
    }

    public boolean addCapturePoint(CapturePoint capturePoint) {
        if(capturePointMap.keySet().contains(capturePoint.getName()))
            return false;

        capturePointMap.put(capturePoint.getName(), capturePoint);
        return true;
    }

    public void removeCapturePoint(String capturePointName) {
        if(!capturePointMap.keySet().contains(capturePointName))
            return;

        capturePointMap.remove(capturePointName);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.HALF_SECOND))
            return;

        capturePointMap.values().forEach(CapturePoint::update);
    }
}
