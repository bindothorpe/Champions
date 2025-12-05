package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.*;
import com.bindothorpe.champions.events.update.Updater;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ChampionsPlugin extends JavaPlugin {

    private DomainController dc;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dc = new DomainController(this);
        InitDataConfig dataConfig = new InitDataConfig(dc);
        dataConfig.initialize();

        Objects.requireNonNull(getCommand("build")).setExecutor(new BuildCommand(dc));
        Objects.requireNonNull(getCommand("team")).setExecutor(new TeamCommand(dc));
        Objects.requireNonNull(getCommand("game")).setExecutor(new GameCommand(dc));
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(dc));
        Objects.requireNonNull(getCommand("cp")).setExecutor(new CapturePointCommand(dc));
        Objects.requireNonNull(getCommand("map")).setExecutor(new GameMapCommand(dc));
        Objects.requireNonNull(getCommand("sound")).setExecutor(new SoundCommand());
        Updater.getInstance(dc).start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        dc.getGameMapManager().unloadMap();
    }
}
