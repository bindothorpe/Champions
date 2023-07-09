package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;

public class GameMap implements IGameMap {
    private final String name;
    private final File sourceWorldFolder;
    private File activeWorldFolder;
    private World bukkitWorld;

    public GameMap(File worldFolder, String worldName, boolean loadOnInit) {
        this.sourceWorldFolder = new File(worldFolder, worldName);
        this.name = worldName;
        if(loadOnInit) load();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean load() {
        if(isLoaded()) return true;

        this.activeWorldFolder = new File(
                Bukkit.getWorldContainer().getParentFile(),
                sourceWorldFolder.getName() + "_active_" + System.currentTimeMillis()
        );

        try {
            FileUtil.copy(sourceWorldFolder, activeWorldFolder);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to copy world folder: " + sourceWorldFolder.getName());
            e.printStackTrace();
            return false;
        }

        this.bukkitWorld = Bukkit.createWorld(
                new WorldCreator(activeWorldFolder.getName())
        );

        if(bukkitWorld != null) this.bukkitWorld.setAutoSave(false);
        return isLoaded();
    }

    @Override
    public void unload() {
        if(bukkitWorld != null) Bukkit.unloadWorld(bukkitWorld, false);
        if(activeWorldFolder != null) FileUtil.delete(activeWorldFolder);

        bukkitWorld = null;
        activeWorldFolder = null;
    }

    @Override
    public boolean reload() {
        unload();
        return load();
    }

    @Override
    public boolean isLoaded() {
        return getWorld() != null;
    }

    @Override
    public World getWorld() {
        return bukkitWorld;
    }
}
