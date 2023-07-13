package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkillUseEvent extends SkillEvent implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    public SkillUseEvent(Player player, SkillId skillId, int skillLevel) {
        super(player, skillId, skillLevel);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
