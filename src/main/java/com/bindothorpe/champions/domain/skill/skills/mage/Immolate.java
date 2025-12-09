package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.FlameItem;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerDropItemWrapperEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Immolate extends Skill implements ReloadableData {

    private static double BASE_ACTIVE_DURATION;
    private static double ACTIVE_DURATION_INCREASE_PER_LEVEL;
    private static double DAMAGE_DONE_MOD;
    private static double DAMAGE_RECEIVED_MOD;
    private static double MOVE_SPEED_MOD;
    private static double BASE_FLAME_DAMAGE;
    private static double FLAME_DAMAGE_INCREASE_PER_LEVEL;

    private final Map<UUID, Long> activeEndDuration = new HashMap<>();

    public Immolate(DomainController dc) {
        super(dc, "Immolate", SkillId.IMMOLATE, SkillType.PASSIVE_A, ClassType.MAGE);
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemWrapperEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;

        if(!event.isWeapon()) return;

        if(!activeEndDuration.containsKey(event.getPlayer().getUniqueId())) {
            enableImmolate(event.getPlayer().getUniqueId(), event);
        } else {
            disableImmolate(event.getPlayer().getUniqueId());
        }
    }

    private void enableImmolate(@NotNull UUID uuid, PlayerDropItemWrapperEvent event) {
        if(!activate(uuid, event)) return;

        double activeDuration = calculateBasedOnLevel(BASE_ACTIVE_DURATION, ACTIVE_DURATION_INCREASE_PER_LEVEL, getSkillLevel(uuid));

        activeEndDuration.put(uuid, System.currentTimeMillis() + ((long) (activeDuration * 1000L)));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.ATTACK_DAMAGE_DONE,
                DAMAGE_DONE_MOD,
                -1,
                true,
                false,
                this));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.DAMAGE_RECEIVED,
                DAMAGE_RECEIVED_MOD,
                -1,
                true,
                false,
                this
        ));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.MOVEMENT_SPEED,
                MOVE_SPEED_MOD,
                -1.0,
                false,
                false,
                this));
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);

    }

    private void disableImmolate(@NotNull UUID uuid) {
        activeEndDuration.remove(uuid);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.ATTACK_DAMAGE_DONE, this);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.DAMAGE_RECEIVED, this);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED, this);
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);
    }

    @EventHandler
    public void onTick(UpdateEvent event) {
        if(event.getUpdateType() != UpdateType.TICK) return;

        for(UUID uuid: getUsers()) {
            if(!activeEndDuration.containsKey(uuid)) continue;

            if(activeEndDuration.get(uuid) < System.currentTimeMillis()) {
                disableImmolate(uuid);
                continue;
            }

            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            player.setFireTicks(0);
            spawnFlames(player);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if(!activeEndDuration.containsKey(event.getEntity().getUniqueId())) return;

        event.setCancelled(true);
    }

    private void spawnFlames(@NotNull Player player) {
        double flameDamage = calculateBasedOnLevel(BASE_FLAME_DAMAGE, FLAME_DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId()));

        GameItem flameItem = new FlameItem(dc,
                player,
                flameDamage,
                3.0D,
                getId());

        dc.getGameItemManager().spawnGameItem(
                flameItem,
                player.getLocation().clone().add(0, 0.5, 0),
                new Vector(Math.random() - 0.5D, Math.random(), Math.random() - 0.5D),
                0.33);

        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_IMMOLATE_AMBIENT);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @Override
    public void onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.immolate.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.immolate.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.cooldown_reduction_per_level");
            BASE_ACTIVE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.base_active_duration");
            ACTIVE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.active_duration_increase_per_level");
            DAMAGE_DONE_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.damage_done_mod");
            DAMAGE_RECEIVED_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.damage_received_mod");
            MOVE_SPEED_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.move_speed_mod");
            BASE_FLAME_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.base_flame_damage");
            FLAME_DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.immolate.flame_damage_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}