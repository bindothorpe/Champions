package com.bindothorpe.champions.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class CustomConfig {

    private static File file;
    private static FileConfiguration customFile;
    private final String name;

    public CustomConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setup() {
        if (isLoaded())
            return;

        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Champions").getDataFolder(), name + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Couldn't create file");
                e.printStackTrace();
            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    private boolean isLoaded() {
        return customFile != null;
    }

    public FileConfiguration getFile() {
        return customFile;
    }

    public void saveFile() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            System.out.println("Couldn't save file");
        }
    }

    public void reloadFile() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }
}
