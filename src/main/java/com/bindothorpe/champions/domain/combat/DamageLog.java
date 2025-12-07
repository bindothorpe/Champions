package com.bindothorpe.champions.domain.combat;

import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.damage.CustomDamageSource;

import java.util.UUID;

public record DamageLog(CustomDamageSource damageSource,UUID receiver, UUID attacker, long timeStamp, String damageSourceString) {
}
