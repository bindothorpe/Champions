package com.bindothorpe.champions.events.cooldown;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CooldownEndEvent extends CooldownEvent {
    public CooldownEndEvent(UUID uuid, Object source) {
        super(uuid, source);
    }
}
