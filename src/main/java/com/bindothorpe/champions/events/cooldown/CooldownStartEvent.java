package com.bindothorpe.champions.events.cooldown;

import java.util.UUID;

public class CooldownStartEvent extends CooldownEvent{
    public CooldownStartEvent(UUID uuid, Object source) {
        super(uuid, source);
    }
}
