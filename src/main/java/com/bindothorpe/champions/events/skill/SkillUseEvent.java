package com.bindothorpe.champions.events.skill;

import com.bindothorpe.champions.domain.skill.SkillId;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkillUseEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private final SkillId skillId;
    private final int skillLevel;
    private @Nullable Component cancelReason;

    public SkillUseEvent(Player player, SkillId skillId, int skillLevel) {
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
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public void setCancelled(boolean cancel, Component reason) {
        setCancelled(cancel);
        this.cancelReason = reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public @Nullable Component getCancelReason() {
        return cancelReason;
    }
}
