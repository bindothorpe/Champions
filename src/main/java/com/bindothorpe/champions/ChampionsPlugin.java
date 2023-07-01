package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.*;
import com.bindothorpe.champions.database.Database;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
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
        getCommand("shop").setExecutor(new ShopCommand(dc));
        Updater.getInstance(dc).start();


        CustomItemManager customItemManager = CustomItemManager.getInstance(dc);
        int tier1 = customItemManager.getTier(CustomItemId.LONG_SWORD);
        int tier2 = customItemManager.getTier(CustomItemId.SERRATED_DIRK);
        int tier3 = customItemManager.getTier(CustomItemId.DUSK_BLADE);

        System.out.println("Long Sword: ");
        System.out.println("tier: " + tier1);
        System.out.println("price: " + customItemManager.getTotalPrice(CustomItemId.LONG_SWORD));
        System.out.println("Serrated Dirk: ");
        System.out.println("tier: " + tier2);
        System.out.println("price: " + customItemManager.getTotalPrice(CustomItemId.SERRATED_DIRK));
        System.out.println("Dusk Blade: ");
        System.out.println("tier: " + tier3);
        System.out.println("price: " + customItemManager.getTotalPrice(CustomItemId.DUSK_BLADE));


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
