package com.bindothorpe.champions.domain.item.events;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;
import org.bukkit.entity.Entity;

public class GameItemPickupEvent extends GameItemEvent {

    private final Entity entity;
    public GameItemPickupEvent(DomainController dc, GameItem gameItem, Entity entity) {
        super(dc, gameItem);
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
