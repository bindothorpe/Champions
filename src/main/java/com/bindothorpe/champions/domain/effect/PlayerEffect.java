package com.bindothorpe.champions.domain.effect;

import com.bindothorpe.champions.domain.skill.SkillId;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class PlayerEffect implements Comparable<PlayerEffect>{

    private UUID id;
    private PlayerEffectType type;
    private double value;
    private double duration;
    private boolean multiply;
    private SkillId source;

    public PlayerEffect(PlayerEffectType type, double value, double duration, boolean multiply, SkillId source) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.value = value;
        this.duration = duration;
        this.multiply = multiply;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public PlayerEffectType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getDuration() {
        return duration;
    }

    public boolean isMultiply() {
        return multiply;
    }

    public SkillId getSource() {
        return source;
    }

    @Override
    public int compareTo(@NotNull PlayerEffect playerEffect) {
        if(isMultiply() == playerEffect.isMultiply())
            return 0;

        if(isMultiply())
            return -1;

        if(playerEffect.isMultiply())
            return 1;

        return 0;
    }

    public abstract void applyEffect(UUID uuid, List<PlayerEffect> playerEffectList);


}
