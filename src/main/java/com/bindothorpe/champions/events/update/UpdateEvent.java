package com.bindothorpe.champions.events.update;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UpdateEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final UpdateType updateType;

    public UpdateEvent(UpdateType updateType) {
        this.updateType = updateType;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
