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
        CustomConfig config = new MapConfig();
        if(loadConfig(new MapConfig())) {
            config.getFile().options().copyDefaults(true);
            config.saveFile();
            dc.getPlugin().getLogger().info("Loaded map config.");
        }
        if(loadConfig(new SkillConfig())) {
            dc.getPlugin().getLogger().info("Loaded skill config.");
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


}
