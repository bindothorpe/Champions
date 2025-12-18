package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
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
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Void extends Skill implements ReloadableData {

    private static double BASE_ACTIVE_DURATION;
    private static double ACTIVE_DURATION_INCREASE_PER_LEVEL;
    private static double BASE_DURATION_REDUCTION_ON_HIT;
    private static double DURATION_REDUCTION_ON_HIT_REDUCTION_PER_LEVEL;
    private static double DAMAGE_RECEIVED_MOD;
    private static int SLOW_MOD;

    private final Map<UUID, Long> activeEndDuration = new HashMap<>();

    public Void(DomainController dc) {
        super(dc,"Void", SkillId.VOID, SkillType.PASSIVE_A, ClassType.MAGE);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        disableVoid(uuid);
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

        activeEndDuration.put(uuid, System.currentTimeMillis() + ((long) (calculateBasedOnLevel(BASE_ACTIVE_DURATION, ACTIVE_DURATION_INCREASE_PER_LEVEL, getSkillLevel(uuid)) * 1000L)));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.KNOCKBACK_RECEIVED,
                0,
                -1,
                true,
                true,
                this));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.DAMAGE_RECEIVED,
                DAMAGE_RECEIVED_MOD,
                -1,
                true,
                false,
                this
        ));
        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.SLOW, uuid, getNamespacedKey(uuid), SLOW_MOD);
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

        activeEndDuration.put(uuid, activeEndDuration.get(uuid) - ((long) (calculateBasedOnLevel(BASE_DURATION_REDUCTION_ON_HIT, -DURATION_REDUCTION_ON_HIT_REDUCTION_PER_LEVEL, getSkillLevel(uuid)) * 1000L)));
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

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.void.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.void.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.cooldown_reduction_per_level");
            BASE_ACTIVE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.base_active_duration");
            ACTIVE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.active_duration_increase_per_level");
            BASE_DURATION_REDUCTION_ON_HIT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.base_duration_reduction_on_hit");
            DURATION_REDUCTION_ON_HIT_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.duration_reduction_on_hit_reduction_per_level");
            DAMAGE_RECEIVED_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.void.damage_received_mod");
            SLOW_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.void.slow_mod");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
