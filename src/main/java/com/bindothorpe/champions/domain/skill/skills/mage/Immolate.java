package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.FlameItem;
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

public class Immolate extends Skill {

    private static final List<Double> activeDuration = List.of(4D, 5D, 6D);

    private final Map<UUID, Long> activeEndDuration = new HashMap<>();

    public Immolate(DomainController dc) {
        super(dc, SkillId.IMMOLATE, SkillType.PASSIVE_A, ClassType.MAGE, "Immolate", List.of(15D, 13D, 11D), 3, 1);
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

        activeEndDuration.put(uuid, System.currentTimeMillis() + ((long) (activeDuration.get(getSkillLevel(uuid) - 1) * 1000L)));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.ATTACK_DAMAGE_DONE,
                1,
                -1,
                true,
                false,
                this));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.DAMAGE_RECEIVED,
                1,
                -1,
                true,
                false,
                this
        ));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.MOVEMENT_SPEED,
                0.2,
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
        GameItem flameItem = new FlameItem(dc,
                player,
                1,
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
}
