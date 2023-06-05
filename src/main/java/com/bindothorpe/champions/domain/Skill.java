package com.bindothorpe.champions.domain;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Skill implements Listener {

    private Map<UUID, Integer> users;

    private SkillId id;
    private SkillType skillType;
    private ClassType classType;
    private String name;
    private List<String> description;
    private int maxLevel;

    public Skill(SkillId id, SkillType skillType, ClassType classType, String name, List<String> description, int maxLevel) {
        this.id = id;
        this.skillType = skillType;
        this.classType = classType;
        this.name = name;
        this.description = description;
        this.maxLevel = maxLevel;
        this.users = new HashMap<>();
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

    public final void activate(UUID uuid) {
        if(!canUse(uuid))
            return;

        Player player = Bukkit.getPlayer(uuid);
        player.sendMessage(String.format("You used %s level %d", this.name, this.users.get(uuid)));
    }

    private final boolean canUse(UUID uuid) {

        if(!users.containsKey(uuid))
            return false;

        /*
        if(isOnCooldown(UUID uuid)) {
            print(cooldown remaining)
            return false;
        }
         */

        return canUseHook(uuid);
    }

    protected final boolean isUser(UUID uuid) {
        return users.containsKey(uuid);
    }

    protected boolean canUseHook(UUID uuid) {
        return true;
    }

}
