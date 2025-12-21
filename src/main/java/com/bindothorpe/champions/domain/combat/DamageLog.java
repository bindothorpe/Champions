package com.bindothorpe.champions.domain.combat;

import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DamageLog(CustomDamageEvent.DamageCause damageCause, UUID receiver, @Nullable UUID attacker, long timeStamp, String damageSourceString) {
}
