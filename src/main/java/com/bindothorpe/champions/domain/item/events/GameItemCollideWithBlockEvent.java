package com.bindothorpe.champions.domain.item.events;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.item.GameItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class GameItemCollideWithBlockEvent extends GameItemEvent{

    private final Block block;
    public GameItemCollideWithBlockEvent(DomainController dc, GameItem gameItem, Block block) {
        super(dc, gameItem);
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
