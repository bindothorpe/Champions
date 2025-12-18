package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkillUnequipEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final SkillId skillId;

    public SkillUnequipEvent(Player player, SkillId skillId) {
        this.player = player;
        this.skillId = skillId;
    }

    public Player getPlayer() {
        return player;
    }

    public SkillId getSkillId() {
        return skillId;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
