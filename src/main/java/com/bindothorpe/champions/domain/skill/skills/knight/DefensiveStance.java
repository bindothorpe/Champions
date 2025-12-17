package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStartBlockingEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStopBlockingEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerUpdateBlockingEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DefensiveStance extends Skill implements ReloadableData {

    private double BASE_DURATION;
    private double DURATION_INCREASE_PER_LEVEL;
    private double BLOCKING_ANGLE;

    private final Set<UUID> activeBlockingSet = new HashSet<>();

    public DefensiveStance(DomainController dc) {
        super(dc, "Defensive Stance", SkillId.DEFENSIVE_STANCE, SkillType.SWORD, ClassType.KNIGHT);
    }

    @EventHandler
    public void onStartBlocking(PlayerStartBlockingEvent event) {
        if(!canUse(event.getPlayer().getUniqueId(), event).result()) return;

        activeBlockingSet.add(event.getPlayer().getUniqueId());
        ChatUtil.sendMessage(event.getPlayer(), ChatUtil.Prefix.SKILL, Component.text("You started ").color(NamedTextColor.GRAY).append(Component.text(getName()).color(NamedTextColor.YELLOW)).append(Component.text(".")));

    }

    @EventHandler
    public void whileBlocking(PlayerUpdateBlockingEvent event) {
        if(!activeBlockingSet.contains(event.getPlayer().getUniqueId())) return;

        if(event.getBlockDuration() <= calculateBasedOnLevel(BASE_DURATION, DURATION_INCREASE_PER_LEVEL, getSkillLevel(event.getPlayer().getUniqueId()))) return;

        onEnd(event.getPlayer());
    }

    @EventHandler
    public void onEndBlocking(PlayerStopBlockingEvent event) {
        if(!activeBlockingSet.contains(event.getPlayer().getUniqueId())) return;

        onEnd(event.getPlayer());
    }


    private void onEnd(@NotNull Player player) {
        activeBlockingSet.remove(player.getUniqueId());
        startCooldown(player.getUniqueId());
        ChatUtil.sendMessage(player, ChatUtil.Prefix.SKILL, Component.text(getName()).color(NamedTextColor.YELLOW).append(Component.text(" ended.").color(NamedTextColor.GRAY)));
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!(event.getDamagee() instanceof Player player)) return;

        if(!activeBlockingSet.contains(player.getUniqueId())) return;

        double angle = getAngle(event.getDamager().getLocation().getDirection(), player.getLocation().getDirection());

        // Check if attack is from the front (within BLOCKING_ANGLE / 2 degrees on either side = BLOCKING_ANGLE degree cone)
        if(angle >= 180 - BLOCKING_ANGLE / 2 && angle <= 180 + BLOCKING_ANGLE / 2) {
            // Attack is from the front, block it
            event.setCancelled(true);
            // Add your blocking logic here (particles, sounds, etc.)
        }

        player.sendMessage(String.format("Angle was %.1f", angle));
    }



    private double getAngle(Vector vector1, Vector vector2) {

        // Project onto horizontal plane (ignore Y component)
        vector1 = vector1.clone().setY(0).normalize();
        vector2 = vector2.clone().setY(0).normalize();

        double dotProduct = vector2.dot(vector1);

        // Convert to angle in degrees
        return Math.toDegrees(Math.acos(dotProduct));
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
            BLOCKING_ANGLE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("blocking_angle"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
