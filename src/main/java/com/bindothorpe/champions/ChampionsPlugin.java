package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.*;
import com.bindothorpe.champions.events.update.Updater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

import java.util.function.Function;

public final class ChampionsPlugin extends JavaPlugin {

    private DomainController dc;

    @Override
    public void onEnable() {
        dc = new DomainController(this);
        InitDataConfig dataConfig = new InitDataConfig(dc);
        dataConfig.initialize();

        getCommand("build").setExecutor(new BuildCommand(dc));
        getCommand("team").setExecutor(new TeamCommand(dc));
        getCommand("skills").setExecutor(new SkillsCommand(dc));
        getCommand("game").setExecutor(new GameCommand(dc));
        getCommand("shop").setExecutor(new ShopCommand(dc));
        getCommand("cp").setExecutor(new CapturePointCommand(dc));
        getCommand("map").setExecutor(new MapCommand(dc));
        getCommand("gamemap").setExecutor(new GameMapCommand(dc));
        Updater.getInstance(dc).start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        dc.getGameMapManager().unloadMap();
    }
}
