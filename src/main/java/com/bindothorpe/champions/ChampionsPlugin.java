package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.*;
import com.bindothorpe.champions.events.update.Updater;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvent;
import io.papermc.paper.registry.event.RegistryEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class ChampionsPlugin extends JavaPlugin {

    private DomainController dc;

    @Override
    public void onEnable() {
        dc = new DomainController(this);
        InitDataConfig dataConfig = new InitDataConfig(dc);
        dataConfig.initialize();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(ChampionsCommand.createCommand(dc).build());
            commands.registrar().register(GameMapCommand.createCommand(dc).build());
        });

        Objects.requireNonNull(getCommand("build")).setExecutor(new BuildCommand(dc));
        Objects.requireNonNull(getCommand("team")).setExecutor(new TeamCommand(dc));
        Objects.requireNonNull(getCommand("game")).setExecutor(new GameCommand(dc));
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(dc));
        Objects.requireNonNull(getCommand("cp")).setExecutor(new CapturePointCommand(dc));
        Objects.requireNonNull(getCommand("sound")).setExecutor(new SoundCommand());
        Updater.getInstance(dc).start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        dc.getGameMapManager().unloadMap();
    }
}
