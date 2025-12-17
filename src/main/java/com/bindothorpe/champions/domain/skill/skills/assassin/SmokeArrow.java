package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SmokeArrow extends Skill implements ReloadableData {

    private final Set<UUID> primed = new HashSet<>();
    private final Set<Arrow> particleTrail = new HashSet<>();

    private static double BASE_BLIND_DURATION;
    private static double BLIND_DURATION_INCREASE_PER_LEVEL;

    public SmokeArrow(DomainController dc) {
        super(dc, "Smoke Arrow", SkillId.SMOKE_ARROW, SkillType.BOW, ClassType.ASSASSIN);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerLeftClickEvent event) {
        boolean success = activate(event.getPlayer().getUniqueId(), event);

        if (!success) {
            return;
        }

        primed.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;

        if (!(arrow.getShooter() instanceof Player player))
            return;

        if (!primed.contains(player.getUniqueId()))
            return;


        arrow.setMetadata("smoke", new FixedMetadataValue(dc.getPlugin(), true));
        primed.remove(player.getUniqueId());

        particleTrail.add(arrow);

    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;

        if (!arrow.hasMetadata("smoke"))
            return;

        if(!(arrow.getShooter() instanceof Player player)) {
            return;
        }

        particleTrail.remove(arrow);

        performSmoke(player, arrow, event.getHitEntity());

    }

    private void performSmoke(Player player, Arrow arrow, Entity hit) {
        if(hit == null) {
            return;
        }
        StatusEffectManager.getInstance(dc).addStatusEffectToEntity(StatusEffectType.BLIND, hit.getUniqueId(), getNamespacedKey(player), 1, calculateBasedOnLevel(BASE_BLIND_DURATION, BLIND_DURATION_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId())));
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getUpdateType() != UpdateType.TICK)
            return;

        for (Arrow arrow : particleTrail) {
            Location loc = arrow.getLocation();
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.BLACK, 1);
            loc.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dustOptions, true);
        }
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerLeftClickEvent e))
            return AttemptResult.FALSE;


        if (!e.isBow())
            return AttemptResult.FALSE;

        if (primed.contains(uuid))
            return new AttemptResult(
                    false,
                    Component.text("You have already primed ", NamedTextColor.GRAY)
                            .append(Component.text(getName(), NamedTextColor.YELLOW))
                            .append(Component.text(".", NamedTextColor.GRAY)),
                    ChatUtil.Prefix.SKILL
            );

        return super.canUseHook(uuid, event);
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_arrow.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_arrow.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_arrow.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_arrow.cooldown_reduction_per_level");
            BASE_BLIND_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_arrow.base_blind_duration");
            BLIND_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_arrow.blind_duration_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
