package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.interact.PlayerDropItemWrapperEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Void extends Skill {


    private static final List<Double> activeDuration = List.of(4D, 5D, 6D);
    private static final List<Double> durationReductionOnHit = List.of(1D, 0.75D, 0.5D);

    private final Map<UUID, Long> activeEndDuration = new HashMap<>();

    public Void(DomainController dc) {
        super(dc, SkillId.VOID, SkillType.PASSIVE_A, ClassType.MAGE, "Void", List.of(15D, 13D, 11D), 3, 1);
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemWrapperEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;

        if(!event.isWeapon()) return;

        if(!activeEndDuration.containsKey(event.getPlayer().getUniqueId())) {
            enableVoid(event.getPlayer().getUniqueId(), event);
        } else {
            disableVoid(event.getPlayer().getUniqueId());
        }
    }

    private void enableVoid(@NotNull UUID uuid, PlayerDropItemWrapperEvent event) {
        if(!activate(uuid, event)) return;

        activeEndDuration.put(uuid, System.currentTimeMillis() + ((long) (activeDuration.get(getSkillLevel(uuid) - 1) * 1000L)));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.KNOCKBACK_RECEIVED,
                0,
                -1,
                true,
                true,
                this));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.DAMAGE_RECEIVED,
                -0.5,
                -1,
                true,
                false,
                this
        ));
        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.SLOW, uuid, getNamespacedKey(uuid), 1);
        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.INVISIBLE, uuid, getNamespacedKey(uuid));

    }

    private void disableVoid(@NotNull UUID uuid) {
        activeEndDuration.remove(uuid);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.KNOCKBACK_RECEIVED, this);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.DAMAGE_RECEIVED, this);
        dc.getStatusEffectManager().removeStatusEffectFromPlayer(StatusEffectType.INVISIBLE, uuid, getNamespacedKey(uuid));
        dc.getStatusEffectManager().removeStatusEffectFromPlayer(StatusEffectType.SLOW, uuid, getNamespacedKey(uuid));
    }

    @EventHandler
    public void onTakeDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(event.getCommand().getDamage() == 0) return;

        UUID uuid = event.getDamagee().getUniqueId();

        if(!isUser(uuid)) return;

        if(!activeEndDuration.containsKey(uuid)) return;

        activeEndDuration.put(uuid, activeEndDuration.get(uuid) - ((long) (durationReductionOnHit.get(getSkillLevel(uuid) - 1) * 1000L)));
        dc.getSoundManager().playSound(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getLocation(), CustomSound.SKILL_VOID_HURT);
    }

    @EventHandler
    public void onTick(UpdateEvent event) {
        if(event.getUpdateType() == UpdateType.TICK) {
            for(UUID uuid: getUsers()) {
                if(!activeEndDuration.containsKey(uuid)) continue;

                if(activeEndDuration.get(uuid) >= System.currentTimeMillis()) continue;

                disableVoid(uuid);

            }
        } else if (event.getUpdateType() == UpdateType.FAST) {
            for(UUID uuid: getUsers()) {
                if(!activeEndDuration.containsKey(uuid)) continue;
                dc.getSoundManager().playSound(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getLocation(), CustomSound.SKILL_VOID_AMBIENT);
            }
        }


    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
