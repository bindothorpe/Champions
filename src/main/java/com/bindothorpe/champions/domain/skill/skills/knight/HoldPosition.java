package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HoldPosition extends Skill implements ReloadableData {

    private final Set<UUID> activeUsers = new HashSet<>();

    private double BASE_DURATION;
    private double DURATION_INCREASE_PER_LEVEL;
    private double BASE_DAMAGE_REDUCTION_PERCENTAGE;
    private double DAMAGE_REDUCTION_PERCENTAGE_INCREASE_PER_LEVEL;

    public HoldPosition(DomainController dc) {
        super(dc, "Hold Position", SkillId.HOLD_POSITION, SkillType.AXE, ClassType.KNIGHT);
    }


    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        UUID uuid = event.getPlayer().getUniqueId();
        double duration = calculateBasedOnLevel(BASE_DURATION, DURATION_INCREASE_PER_LEVEL, getSkillLevel(event.getPlayer()));

        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.SLOW, uuid, getNamespacedKey(uuid), 4, duration);
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.DAMAGE_RECEIVED,
                calculateBasedOnLevel(BASE_DAMAGE_REDUCTION_PERCENTAGE, DAMAGE_REDUCTION_PERCENTAGE_INCREASE_PER_LEVEL, getSkillLevel(uuid)),
                duration,
                true,
                false,
                this
        ));
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.KNOCKBACK_RECEIVED,
                0,
                duration,
                true,
                true,
                this
        ));

        activeUsers.add(uuid);
        new Timer(dc.getPlugin(), duration, () -> activeUsers.remove(uuid)).start();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        for(UUID uuid : activeUsers) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null || !player.isOnline()) continue;

            player.getWorld().spawnParticle(
                    org.bukkit.Particle.EFFECT,
                    player.getLocation().add(0, 1, 0),
                    5,
                    0.3, 0.5, 0.3,
                    0,
                    new Particle.Spell(Color.BLACK, 1)
            );
        }
    }


    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {

        if(!(event instanceof PlayerRightClickEvent rightClickEvent)) return AttemptResult.FALSE;

        if(!rightClickEvent.isAxe()) return AttemptResult.FALSE;

        return super.canUseHook(uuid, event);
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("cooldown_reduction_per_level"));
            BASE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_duration"));
            DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("duration_increase_per_level"));
            BASE_DAMAGE_REDUCTION_PERCENTAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_damage_reduction_percentage"));
            DAMAGE_REDUCTION_PERCENTAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage_reduction_percentage_increase_per_level"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.active()
                .append(ComponentUtil.rightClick(true))
                .append(Component.text("to hold your position, reducing incoming damage by ").color(NamedTextColor.GRAY))
                .append(ComponentUtil.skillValuesBasedOnLevel((int) (BASE_DAMAGE_REDUCTION_PERCENTAGE * 100), (int) (DAMAGE_REDUCTION_PERCENTAGE_INCREASE_PER_LEVEL * 100), skillLevel, MAX_LEVEL, true, NamedTextColor.YELLOW))
                .append(Component.text(", Slow IV and no knockback for ").color(NamedTextColor.GRAY))
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DURATION, DURATION_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                .append(Component.text(" seconds.").color(NamedTextColor.GRAY)),
                35);
    }
}
