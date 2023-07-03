package com.bindothorpe.champions.domain.entityStatus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum EntityStatusType {

    DAMAGE_DONE,
    DAMAGE_RECEIVED,

    ATTACK_DAMAGE_DONE,
    ATTACK_DAMAGE_RECEIVED,

    SKILL_DAMAGE_DONE,
    SKILL_DAMAGE_RECEIVED,

    KNOCKBACK_DONE,
    KNOCKBACK_RECEIVED,

    ATTACK_KNOCKBACK_DONE,
    ATTACK_KNOCKBACK_RECEIVED,

    SKILL_KNOCKBACK_DONE,
    SKILL_KNOCKBACK_RECEIVED,

    MOVEMENT_SPEED,
    COOLDOWN_REDUCTION;

}
