package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitingArrow extends Skill implements ReloadableData {

    private static double BASE_SPEED_DURATION;
    private static double SPEED_DURATION_INCREASE_PER_LEVEL;
    private static double BASE_MOVE_SPEED_MOD;
    private static double MOVE_SPEED_MOD_INCREASE_PER_LEVEL;

    public KitingArrow(DomainController dc) {
        super(dc, "Kiting Arrow", SkillId.KITING_ARROW, SkillType.PASSIVE_A, ClassType.RANGER);
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        if (!isUser(player.getUniqueId()))
            return;

        dc.getEntityStatusManager().addEntityStatus(player.getUniqueId(), new EntityStatus(EntityStatusType.MOVEMENT_SPEED, calculateBasedOnLevel(BASE_MOVE_SPEED_MOD, MOVE_SPEED_MOD_INCREASE_PER_LEVEL, getSkillLevel(player)), calculateBasedOnLevel(BASE_SPEED_DURATION, SPEED_DURATION_INCREASE_PER_LEVEL, getSkillLevel(player)), false, false, this));
        dc.getEntityStatusManager().updateEntityStatus(player.getUniqueId(), EntityStatusType.MOVEMENT_SPEED);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("Shooting an arrow gives you").color(NamedTextColor.GRAY)));
        lore.add(ComponentUtil.skillValuesBasedOnLevel((int) (BASE_MOVE_SPEED_MOD * 100), (int) (MOVE_SPEED_MOD_INCREASE_PER_LEVEL * 100), skillLevel, MAX_LEVEL, NamedTextColor.WHITE)
                .append(Component.text(" movement speed for").color(NamedTextColor.GRAY)));
        lore.add(ComponentUtil.skillValuesBasedOnLevel(BASE_SPEED_DURATION, SPEED_DURATION_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.WHITE)
                .append(Component.text(" seconds").color(NamedTextColor.GRAY)));
        return lore;
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.kiting_arrow.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.kiting_arrow.level_up_cost");
            BASE_SPEED_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.kiting_arrow.base_speed_duration");
            SPEED_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.kiting_arrow.speed_duration_increase_per_level");
            BASE_MOVE_SPEED_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.kiting_arrow.base_move_speed_mod");
            MOVE_SPEED_MOD_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.kiting_arrow.move_speed_mod_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
