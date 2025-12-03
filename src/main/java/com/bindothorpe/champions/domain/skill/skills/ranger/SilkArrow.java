package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class SilkArrow extends Skill {

    private final Set<UUID> primed = new HashSet<>();
    private final Set<Arrow> particleTrail = new HashSet<>();
    private final List<Double> duration = Arrays.asList(3.0, 4.0, 5.0);


    public SilkArrow(DomainController dc) {
        super(dc, SkillId.SILK_ARROW, SkillType.BOW, ClassType.RANGER, "Silk Arrow", Arrays.asList(10.0), 1, 2);
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
        if (!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        if (!primed.contains(player.getUniqueId()))
            return;


        arrow.setMetadata("silk", new FixedMetadataValue(dc.getPlugin(), true));
        primed.remove(player.getUniqueId());

        particleTrail.add(arrow);

    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!arrow.hasMetadata("silk"))
            return;

        if(!(arrow.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) arrow.getShooter();

        particleTrail.remove(arrow);

        performSilk(player, arrow, event.getHitEntity());

    }

    private void performSilk(Player player, Arrow arrow, Entity hit) {
        List<Vector> vectors = ShapeUtil.sphere(3).stream().sorted((v1, v2) -> v2.getBlockY() - v1.getBlockY()).collect(Collectors.toList());

        Location loc = hit != null ? hit.getLocation() : arrow.getLocation();

        for(int i = 0; i < vectors.size(); i++) {
            Vector v = vectors.get(i);
            double finalDuration = duration.get(getSkillLevel(player.getUniqueId())) + (i / 500D);

            dc.getTemporaryBlockManager().spawnTemporaryBlock(
                    loc.clone().add(v),
                    Material.COBWEB,
                    finalDuration
            );
        }

        arrow.remove();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getUpdateType() != UpdateType.TICK)
            return;

        for (Arrow arrow : particleTrail) {
            Location loc = arrow.getLocation();

            if(!(arrow.getShooter() instanceof Player)) {
                return;
            }

            Player player = (Player) arrow.getShooter();

            if(player.getLocation().distance(arrow.getLocation()) <= 2) {
                return;
            }

            dc.getTemporaryBlockManager().spawnTemporaryBlock(loc, Material.COBWEB, duration.get(getSkillLevel(player.getUniqueId()) - 1));
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
