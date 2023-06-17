package com.bindothorpe.champions.events.build;

import com.bindothorpe.champions.domain.build.Build;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BuildEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Build build;

    public BuildEvent(Build build) {
        this.build = build;
    }

    public Build getBuild() {
        return build;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
