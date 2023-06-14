package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class Skill implements Listener {

    private Map<UUID, Integer> users;
    private Map<UUID, Long> cooldownMap;

    protected DomainController dc;
    private SkillId id;
    private SkillType skillType;
    private ClassType classType;
    private String name;
    private List<Double> cooldownDuration;
    private int maxLevel;
    private int levelUpCost;


    public Skill(DomainController dc, SkillId id, SkillType skillType, ClassType classType, String name, List<Double> cooldownDuration, int maxLevel, int levelUpCost) {
        this.dc =dc;
        this.id = id;
        this.skillType = skillType;
        this.classType = classType;
        this.name = name;
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

    public abstract List<Component> getDescription(int skillLevel);

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


    protected final boolean activate(UUID uuid, Event event) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return false;

        if (!canUse(uuid, event))
            return false;

        SkillUseEvent skillUseEvent = new SkillUseEvent(player, getId(), users.get(uuid));
        Bukkit.getPluginManager().callEvent(skillUseEvent);

        if(skillUseEvent.isCancelled())
            return false;

        player.sendMessage(Component.text("You used ").color(NamedTextColor.GRAY)
                .append(Component.text(this.name).color(NamedTextColor.YELLOW))
                .append(Component.text(" level ").color(NamedTextColor.GRAY))
                .append(Component.text(this.users.get(uuid)).color(NamedTextColor.YELLOW)));
        startCooldown(uuid);
        return true;
    }

    private final boolean canUse(UUID uuid, Event event) {

        if (!users.containsKey(uuid))
            return false;


        if (isOnCooldown(uuid)) {
            double cooldownRemaining = getCooldownRemaining(uuid);
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage(Component.text("You cannot use this skill for another ").color(NamedTextColor.GRAY)
                    .append(Component.text(cooldownRemaining).color(NamedTextColor.YELLOW))
                    .append(Component.text(" seconds").color(NamedTextColor.GRAY)));
            return false;
        }


        return canUseHook(uuid, event);
    }

    private boolean isOnCooldown(UUID uuid) {
        if (!cooldownMap.containsKey(uuid))
            return false;

        long cooldownStart = cooldownMap.get(uuid);
        long cooldownDuration = (long) (this.cooldownDuration.get(users.get(uuid) - 1) * 1000);

        return System.currentTimeMillis() - cooldownStart < cooldownDuration;
    }

    private double getCooldownRemaining(UUID uuid) {
        if (!cooldownMap.containsKey(uuid))
            return 0;

        long cooldownStart = cooldownMap.get(uuid);
        long cooldownDuration = (long) (this.cooldownDuration.get(users.get(uuid) - 1) * 1000);

        return (cooldownStart + cooldownDuration - System.currentTimeMillis()) / 1000.0;
    }

    protected boolean canUseHook(UUID uuid, Event event) {
        return true;
    }

    protected boolean isUser(UUID uuid) {
        return users.containsKey(uuid);
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public ClassType getClassType() {
        return classType;
    }

    protected int getSkillLevel(UUID uuid) {
        return users.get(uuid);
    }

    protected Set<UUID> getUsers() {
        return users.keySet();
    }
}
