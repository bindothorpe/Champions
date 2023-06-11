package com.bindothorpe.champions.domain.effect.effects;

import com.bindothorpe.champions.domain.effect.PlayerEffect;
import com.bindothorpe.champions.domain.effect.PlayerEffectType;
import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovementSpeedPlayerEffect extends PlayerEffect {

    private static final float DEFAULT_MOVEMENT_SPEED = 0.2F;

    public MovementSpeedPlayerEffect(double value, double duration, boolean multiply, SkillId source) {
        super(PlayerEffectType.MOVEMENT_SPEED, value, duration, multiply, source);
    }

    @Override
    public void applyEffect(UUID uuid, List<PlayerEffect> playerEffectList) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        List<PlayerEffect> modificationEffects = new ArrayList<>();
        List<PlayerEffect> multiplicationEffects = new ArrayList<>();

        for (PlayerEffect playerEffect : playerEffectList) {
            if (playerEffect.isMultiply()) {
                multiplicationEffects.add(playerEffect);
            } else {
                modificationEffects.add(playerEffect);
            }
        }

        float movementSpeed = DEFAULT_MOVEMENT_SPEED;

        movementSpeed += modificationEffects.stream().reduce(0.0, (a, b) -> a + b.getValue(), Double::sum);
        movementSpeed *= multiplicationEffects.stream().reduce(1.0, (a, b) -> a + b.getValue(), Double::sum);

        movementSpeed = Math.max(0.0F, Math.min(1.0F, movementSpeed));

        player.setWalkSpeed(movementSpeed);
    }

}

