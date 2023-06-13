package com.bindothorpe.champions.domain.effect.effects;

import com.bindothorpe.champions.domain.effect.PlayerEffect;
import com.bindothorpe.champions.domain.effect.PlayerEffectType;
import com.bindothorpe.champions.domain.skill.SkillId;

import java.util.List;
import java.util.UUID;

public class KnockbackReceivedPlayerEffect extends PlayerEffect {

    public KnockbackReceivedPlayerEffect(double value, double duration, boolean multiply, SkillId source) {
        super(PlayerEffectType.KNOCKBACK_RECEIVED, value, duration, multiply, source);
    }

    @Override
    public void applyEffect(UUID uuid, List<PlayerEffect> playerEffectList) {
    }

}

