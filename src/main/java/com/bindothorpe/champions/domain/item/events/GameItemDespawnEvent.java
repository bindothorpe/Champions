package com.bindothorpe.champions.domain.item.events;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;

public class GameItemDespawnEvent extends GameItemEvent{

    public GameItemDespawnEvent(DomainController dc, GameItem gameItem) {
        super(dc, gameItem);
    }
}
