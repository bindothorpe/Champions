package com.bindothorpe.champions.events.game.capturepoint;

import com.bindothorpe.champions.domain.game.capturePoint.CapturePoint;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class CapturePointEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final CapturePoint capturePoint;

    public CapturePointEvent(CapturePoint capturePoint) {
        this.capturePoint = capturePoint;
    }

    public CapturePoint getCapturePoint() {
        return capturePoint;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
