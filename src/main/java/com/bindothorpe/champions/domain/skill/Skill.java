package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.domain.build.ClassType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Skill implements Listener {

    private Map<UUID, Integer> users;
    private Map<UUID, Long> cooldownMap;

    private SkillId id;
    private SkillType skillType;
    private ClassType classType;
    private String name;
    private List<String> description;
    private List<Double> cooldownDuration;
    private int maxLevel;
    private int levelUpCost;


    public Skill(SkillId id, SkillType skillType, ClassType classType, String name, List<String> description, List<Double> cooldownDuration, int maxLevel, int levelUpCost) {
        this.id = id;
        this.skillType = skillType;
        this.classType = classType;
        this.name = name;
        this.description = description;
        this.cooldownDuration = cooldownDuration;
        this.maxLevel = maxLevel;
        this.levelUpCost = levelUpCost;
        this.users = new HashMap<>();
        this.cooldownMap = new HashMap<>();
    }


    public SkillId getId() {
        return id;
    }

    public void addUser(UUID uuid, int skillLevel) {
        users.put(uuid, skillLevel);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<Double> getCooldownDuration() {
        return cooldownDuration;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevelUpCost() {
        return levelUpCost;
    }

    private void startCooldown(UUID uuid) {
        cooldownMap.put(uuid, System.currentTimeMillis());
    }


    protected final void activate(UUID uuid) {
        if(!canUse(uuid))
            return;

        Player player = Bukkit.getPlayer(uuid);
        String message = getActivateMessage(uuid);
        if(message != null) {
            player.sendMessage(message);
        }
        startCooldown(uuid);
    }

    protected String getActivateMessage(UUID uuid) {
        return String.format("You used %s level %d", this.name, this.users.get(uuid));
    }

    private final boolean canUse(UUID uuid) {

        if(!users.containsKey(uuid))
            return false;

//
//        if(isOnCooldown(UUID uuid)) {
//            print(cooldown remaining)
//            return false;
//        }


        return canUseHook(uuid);
    }

    protected boolean canUseHook(UUID uuid) {
        return true;
    }

    protected boolean isUser(UUID uuid) {
        return users.containsKey(uuid);
    }


}
