package com.bindothorpe.champions.events.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLeftClickEvent extends PlayerClickEvent {

    public PlayerLeftClickEvent(Player player) {
        super(player);
    }
}
