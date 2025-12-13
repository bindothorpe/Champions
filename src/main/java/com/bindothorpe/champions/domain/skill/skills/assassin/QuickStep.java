package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
        super(dc, "Quick Step", SkillId.QUICK_STEP, SkillType.PASSIVE_B, ClassType.ASSASSIN);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;
        lastMoveDirectionMap.put(event.getPlayer().getUniqueId(), MobilityUtil.directionTo(event.getFrom(), event.getTo()));
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getDamager() instanceof Player player)) return;

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
