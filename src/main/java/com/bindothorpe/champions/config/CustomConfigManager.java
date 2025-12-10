package com.bindothorpe.champions.config;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.config.game.map.MapConfig;
import com.bindothorpe.champions.config.skill.SkillConfig;
import com.bindothorpe.champions.domain.skill.Skill;

import java.util.HashMap;
import java.util.Map;

public class CustomConfigManager {

    private static CustomConfigManager instance;
    private final DomainController dc;

    private final Map<String, CustomConfig> customConfigMap = new HashMap<>();

    private CustomConfigManager(DomainController dc) {
        this.dc = dc;
    }

    private void initialize() {
        CustomConfig skillConfig = new SkillConfig();
        if(loadConfig(skillConfig)) {
            skillConfig.getFile().options().copyDefaults(true);
            skillConfig.saveFile();
            dc.getPlugin().getLogger().info("Loaded skill config.");
        }
        CustomConfig mapConfig = new MapConfig();
        if(loadConfig(mapConfig)) {
            mapConfig.getFile().options().copyDefaults(true);
            mapConfig.saveFile();
            dc.getPlugin().getLogger().info("Loaded map config.");
        }
    }

    public static CustomConfigManager getInstance(DomainController dc) {
        if(instance == null) instance = new CustomConfigManager(dc);
        return instance;
    }

    public CustomConfig getConfig(String name) {
        if(customConfigMap.isEmpty()) {
            initialize();
        }

        return customConfigMap.get(name);
    }

    private boolean loadConfig(CustomConfig config) {
        if(config == null)
            return false;

        String name = config.getName();

        if(customConfigMap.containsKey(name))
            return false;

        customConfigMap.put(name, config);
        config.setup();
        return true;
    }

    public void reloadConfig(String name) {
        CustomConfig config = customConfigMap.get(name);
        if(config != null) {
            config.reloadFile();
            dc.getPlugin().getLogger().info("Reloaded " + name + " config.");
        } else {
            dc.getPlugin().getLogger().warning("Config " + name + " not found.");
        }
    }

    public void reloadAllConfigs() {
        if(customConfigMap.isEmpty()) {
            dc.getPlugin().getLogger().warning("No configs loaded to reload.");
            return;
        }

        for(Map.Entry<String, CustomConfig> entry : customConfigMap.entrySet()) {
            entry.getValue().reloadFile();
            dc.getPlugin().getLogger().info("Reloaded " + entry.getKey() + " config.");
        }
    }


}
