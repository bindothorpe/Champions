package com.bindothorpe.champions;

import com.bindothorpe.champions.commands.*;
import com.bindothorpe.champions.events.update.Updater;
import com.bindothorpe.champions.protocol.ShieldPacketListener;
import com.bindothorpe.champions.util.ChatUtil;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ChampionsPlugin extends JavaPlugin {

    private DomainController dc;

    @Override
    public void onEnable() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        if(protocolManager == null) {
            ChatUtil.sendBroadcast(ChatUtil.Prefix.ERROR, Component.text("ProtocolLib not found!").color(NamedTextColor.WHITE));
            getServer().getConsoleSender().sendMessage(Component.text("ProtocolLib not found!").color(NamedTextColor.RED));
        } else {
            ChatUtil.sendBroadcast(ChatUtil.Prefix.PLUGIN, Component.text("ProtocolLib loaded!").color(NamedTextColor.WHITE));
            getServer().getConsoleSender().sendMessage(Component.text("ProtocolLib loaded!").color(NamedTextColor.GREEN));

            protocolManager.addPacketListener(new ShieldPacketListener(this));
        }

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
