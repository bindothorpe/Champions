package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

public class SilkArrow extends Skill {

    private static final double DURATION = 10.0D;

    private final Set<UUID> primed = new HashSet<>();
    private final Set<Arrow> particleTrail = new HashSet<>();
    private final Map<Arrow, Location> lastLocation = new HashMap<>();


    public SilkArrow(DomainController dc) {
        super(dc, SkillId.SILK_ARROW, SkillType.BOW, ClassType.RANGER, "Silk Arrow", List.of(10.0), 1, 2);
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


        arrow.setMetadata("silk", new FixedMetadataValue(dc.getPlugin(), true));
        primed.remove(player.getUniqueId());

        particleTrail.add(arrow);

    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;

        if (!arrow.hasMetadata("silk"))
            return;

        if(!(arrow.getShooter() instanceof Player player)) {
            return;
        }

        particleTrail.remove(arrow);

        performSilk(player, arrow, event.getHitEntity());

    }

    private void performSilk(Player player, Arrow arrow, Entity hit) {
//        List<Vector> vectors = ShapeUtil.sphere(1).stream().sorted((v1, v2) -> v2.getBlockY() - v1.getBlockY()).collect(Collectors.toList());
//
//        Location loc = hit != null ? hit.getLocation() : arrow.getLocation();
//
//        for(int i = 0; i < vectors.size(); i++) {
//            Vector v = vectors.get(i);
//            double finalDuration = duration.get(getSkillLevel(player.getUniqueId())) + (i / 500D);
//
//            dc.getTemporaryBlockManager().spawnTemporaryBlock(
//                    loc.clone().add(v),
//                    Material.COBWEB,
//                    finalDuration
//            );
//        }

        arrow.remove();
        lastLocation.remove(arrow);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getUpdateType() != UpdateType.TICK)
            return;

        for (Arrow arrow : particleTrail) {
            Location currentLoc = arrow.getLocation();

            if(!(arrow.getShooter() instanceof Player player)) {
                continue; // Changed from return to continue
            }

            if(player.getLocation().distance(currentLoc) <= 2) {
                continue; // Changed from return to continue
            }

            // Check if we have a last location for this arrow
            Location lastLoc = lastLocation.get(arrow);

            if(lastLoc != null) {
                // Get all block locations between last and current location
                List<Location> blocksBetween = getBlockLocationsBetween(lastLoc, currentLoc);

                // Place cobwebs at each location
                for(Location loc : blocksBetween) {
                    dc.getTemporaryBlockManager().spawnTemporaryBlock(loc, Material.COBWEB, DURATION);
                }
            } else {
                // First time - just place at current location
                dc.getTemporaryBlockManager().spawnTemporaryBlock(currentLoc, Material.COBWEB, DURATION);
            }

            // Update last location
            lastLocation.put(arrow, currentLoc.clone());
        }
    }

    private List<Location> getBlockLocationsBetween(Location start, Location end) {
        List<Location> locations = new ArrayList<>();

        // Make sure both locations are in the same world
        if(!start.getWorld().equals(end.getWorld())) {
            return locations;
        }

        // Get the vector between the two points
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        // Step through the line and collect block locations
        for(double i = 0; i <= distance; i += 0.5) { // 0.5 block intervals
            Location loc = start.clone().add(direction.clone().multiply(i));
            Location blockLoc = loc.getBlock().getLocation();

            // Avoid duplicates by checking if we already added this block
            if(locations.isEmpty() || !blockLoc.equals(locations.get(locations.size() - 1))) {
                locations.add(blockLoc);
            }
        }

        return locations;
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

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(ComponentUtil.active()
                .append(ComponentUtil.leftClick())
                .append(Component.text("to prime").color(NamedTextColor.GRAY)));
        lore.add(Component.text("your next arrow.").color(NamedTextColor.GRAY));
        lore.add(Component.text(" "));
        lore.add(Component.text("Your next arrow will leave").color(NamedTextColor.GRAY));
        lore.add(Component.text("behind a trail of cobwebs").color(NamedTextColor.GRAY));
        lore.add(Component.text("that will remain for ").color(NamedTextColor.GRAY).append(ComponentUtil.skillLevelValues(skillLevel, List.of(DURATION), NamedTextColor.YELLOW)));
        lore.add(Component.text("seconds.").color(NamedTextColor.GRAY));

        return lore;
    }
}
