package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RopedArrow extends Skill implements ReloadableData {

    private final Set<UUID> primed = new HashSet<>();
    private final Set<UUID> arrows = new HashSet<>();

    public RopedArrow(DomainController dc) {
        super(dc, "Roped Arrow", SkillId.ROPED_ARROW, SkillType.BOW, ClassType.RANGER);
    }

    @EventHandler
    public void onLeftClick(PlayerLeftClickEvent event) {
        Player player = event.getPlayer();
        if(!activate(player.getUniqueId(), event)) return;

        primed.add(player.getUniqueId());
        dc.getSoundManager().playSound(player, CustomSound.SKILL_ROPED_ARROW_PRIME);
    }

    @EventHandler
    public void handleBowShoot(EntityShootBowEvent event) {
        if(!(event.getProjectile() instanceof Arrow arrow)) return;

        if(!(event.getEntity() instanceof Player player)) return;

        if(!isUser(player.getUniqueId())) return;

        if(!primed.remove(player.getUniqueId())) return;

        //TODO: You fired X message
        arrows.add(arrow.getUniqueId());
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof Arrow arrow)) return;

        if(!arrows.remove(arrow.getUniqueId())) return;

        if(!(arrow.getShooter() instanceof Player player)) return;

        if(!isUser(player.getUniqueId())) return;

        Vector direction = MobilityUtil.directionTo(player.getLocation(), arrow.getLocation()).normalize();
        double multiplier = arrow.getVelocity().length() / 1.2D;
        System.out.println(arrow.getVelocity().length());
        MobilityUtil.launch(
                player,
                direction,
                0.6D * multiplier,  // Increased from 0.4
                false,
                0.0D,
                0.1D * multiplier,
                1.2D * multiplier,
                true);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_ROPED_ARROW_PRIME);

    }


    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerLeftClickEvent e))
            return false;


        if (dc.getTeamManager().getTeamFromEntity(e.getPlayer()) == null) {
            return false;
        }


        if (!e.isBow())
            return false;


        if (primed.contains(uuid))
            return false;


        return super.canUseHook(uuid, event);
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.roped_arrow.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.roped_arrow.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.roped_arrow.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.roped_arrow.cooldown_reduction_per_level");
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
