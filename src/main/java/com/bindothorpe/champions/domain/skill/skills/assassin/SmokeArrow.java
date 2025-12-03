package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import net.kyori.adventure.text.Component;
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

public class SmokeArrow extends Skill {

    private final Set<UUID> primed = new HashSet<>();
    private final Set<Arrow> particleTrail = new HashSet<>();

    public SmokeArrow(DomainController dc) {
        super(dc, SkillId.SMOKE_ARROW, SkillType.BOW, ClassType.ASSASSIN, "Smoke Arrow", List.of(20D, 18D, 16D, 14D, 12D), 5, 1);
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
        StatusEffectManager.getInstance(dc).addStatusEffectToEntity(StatusEffectType.BLIND, hit.getUniqueId(), getNamespacedKey(player), 1, -1);
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
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerLeftClickEvent))
            return false;

        PlayerLeftClickEvent e = (PlayerLeftClickEvent) event;


        if (dc.getTeamManager().getTeamFromEntity(e.getPlayer()) == null) {
            return false;
        }

        if (!e.isBow())
            return false;

        if (primed.contains(uuid))
            return false;

        return super.canUseHook(uuid, event);
    }
}
