package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuickStep extends Skill implements ReloadableData {

    private double LAUNCH_STRENGTH;

    private final Map<UUID, Vector> lastMoveDirectionMap = new HashMap<>();

    public QuickStep(DomainController dc) {
        super(dc, "Quick Step", SkillId.QUICK_STEP, SkillType.PASSIVE_B, ClassType.RANGER);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        lastMoveDirectionMap.remove(uuid);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;

        // Check if player actually moved position (not just rotated view)
        if(event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            // Player didn't move horizontally - clear their direction
            lastMoveDirectionMap.remove(event.getPlayer().getUniqueId());
            return;
        }

        // Player is moving - store their direction
        lastMoveDirectionMap.put(event.getPlayer().getUniqueId(),
                MobilityUtil.directionTo(event.getFrom(), event.getTo()));
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if(!(event.getEntity() instanceof Arrow arrow)) return;

        if(!(arrow.getShooter() instanceof Player player)) return;

        if(!isUser(player)) return;

        if(!lastMoveDirectionMap.containsKey(player.getUniqueId())) return;

        MobilityUtil.launch(player,
                lastMoveDirectionMap.get(player.getUniqueId()),
                LAUNCH_STRENGTH,
                false,
                0.0D,
                0.0D,
                0.0D,
                true
        );
    }



    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("launch_strength"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
