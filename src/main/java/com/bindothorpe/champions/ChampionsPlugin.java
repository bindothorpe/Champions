package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.BuildCommand;
import com.bindothorpe.champions.commands.GameCommand;
import com.bindothorpe.champions.commands.SkillsCommand;
import com.bindothorpe.champions.commands.TeamCommand;
import com.bindothorpe.champions.database.Database;
import com.bindothorpe.champions.events.update.Updater;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class ChampionsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        DomainController dc = new DomainController(this);
        InitDataConfig dataConfig = new InitDataConfig(dc);
        dataConfig.initialize();

        getCommand("build").setExecutor(new BuildCommand(dc));
        getCommand("team").setExecutor(new TeamCommand(dc));
        getCommand("skills").setExecutor(new SkillsCommand(dc));
        getCommand("game").setExecutor(new GameCommand(dc));
        Updater.getInstance(dc).start();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
