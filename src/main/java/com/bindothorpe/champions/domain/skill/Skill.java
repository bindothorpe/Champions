package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.events.cooldown.CooldownEndEvent;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class Skill implements Listener {

    private Map<UUID, Integer> users;
//    private Map<UUID, Long> cooldownMap;

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
//        this.cooldownMap = new HashMap<>();
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
        double duration = 0;
        if(cooldownDuration == null || cooldownDuration.isEmpty())
            return;
        try {
            duration = cooldownDuration.get(users.get(uuid) - 1);
        } catch (IndexOutOfBoundsException e) {
            duration = cooldownDuration.get(0);
        }

        if(duration == 0)
            return;

        double cooldownMultiplier = dc.getEntityStatusManager().getMultiplicationValue(uuid, EntityStatusType.COOLDOWN_REDUCTION) - 1;
        double cooldownReduction = duration * cooldownMultiplier;

        dc.getCooldownManager().startCooldown(uuid, this, duration - cooldownReduction);
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

        ChatUtil.sendMessage(player, ChatUtil.Prefix.SKILL, Component.text("You used ").color(NamedTextColor.GRAY)
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

            if(player == null)
                return false;

            ChatUtil.sendMessage(player, ChatUtil.Prefix.COOLDOWN, Component.text("You cannot use this skill for another ").color(NamedTextColor.GRAY)
                    .append(Component.text(cooldownRemaining).color(NamedTextColor.YELLOW))
                    .append(Component.text(" seconds").color(NamedTextColor.GRAY)));
            return false;
        }


        return canUseHook(uuid, event);
    }

    private boolean isOnCooldown(UUID uuid) {
        return dc.getCooldownManager().isOnCooldown(uuid, this);
    }

    private double getCooldownRemaining(UUID uuid) {
        return dc.getCooldownManager().getCooldownRemaining(uuid, this);
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

    @EventHandler
    public void onCooldownEnd(CooldownEndEvent event) {
        if(!equals(event.getSource()))
            return;

        Player player = Bukkit.getPlayer(event.getUuid());

        if(player == null)
            return;

        ChatUtil.sendMessage(player, ChatUtil.Prefix.COOLDOWN, Component.text("You can use ").color(NamedTextColor.GRAY)
                .append(Component.text(this.name).color(NamedTextColor.YELLOW))
                .append(Component.text(" again").color(NamedTextColor.GRAY)));
    }
}
