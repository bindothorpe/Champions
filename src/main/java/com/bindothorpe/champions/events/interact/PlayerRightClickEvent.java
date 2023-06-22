package com.bindothorpe.champions.events.interact;

import com.bindothorpe.champions.domain.item.events.GameItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerRightClickEvent extends PlayerClickEvent {

    public PlayerRightClickEvent(Player player) {
        super(player);
    }
}
