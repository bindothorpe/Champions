package com.bindothorpe.champions.gui;

import com.bindothorpe.champions.DomainController;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import org.bukkit.Bukkit;

import java.util.UUID;

public abstract class PlayerGui {
    
    protected UUID uuid;
    protected DomainController dc;
    protected ChestGui gui;

    public PlayerGui(UUID uuid, DomainController dc) {
        this.uuid = uuid;
        this.dc = dc;
    }

    protected abstract void initialize();

    public final void open() {
        gui.setOnTopClick(event -> event.setCancelled(true));
        gui.setOnTopDrag(event -> event.setCancelled(true));
        gui.show(Bukkit.getPlayer(uuid));
    }
}
