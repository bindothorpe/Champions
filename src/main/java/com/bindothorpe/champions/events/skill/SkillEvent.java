package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkillEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final SkillId skillId;
    private final int skillLevel;

    public SkillEvent(Player player, SkillId skillId, int skillLevel) {
        this.player = player;
        this.skillId = skillId;
        this.skillLevel = skillLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public SkillId getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
