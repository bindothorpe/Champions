package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.*;
import com.bindothorpe.champions.database.Database;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.events.update.Updater;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class ChampionsPlugin extends JavaPlugin {

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        DomainController dc = new DomainController(this, protocolManager);
        InitDataConfig dataConfig = new InitDataConfig(dc);
        dataConfig.initialize();

        getCommand("build").setExecutor(new BuildCommand(dc));
        getCommand("team").setExecutor(new TeamCommand(dc));
        getCommand("skills").setExecutor(new SkillsCommand(dc));
        getCommand("game").setExecutor(new GameCommand(dc));
        getCommand("shop").setExecutor(new ShopCommand(dc));
        Updater.getInstance(dc).start();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
