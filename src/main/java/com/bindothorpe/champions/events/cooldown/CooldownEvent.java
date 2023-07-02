package com.bindothorpe.champions.events.cooldown;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CooldownEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final UUID uuid;
    private final Object source;

    public CooldownEvent(UUID uuid, Object source) {
        this.uuid = uuid;
        this.source = source;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Object getSource() {
        return source;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
