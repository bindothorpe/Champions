package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BouncingArrow extends Skill {

    private List<Integer> bounce = Arrays.asList(1, 2, 3);
    private List<Double> distance = Arrays.asList(5D, 8D, 14D);

    public BouncingArrow(DomainController dc) {
        super(dc, SkillId.BOUNCING_ARROW, SkillType.PASSIVE_B, ClassType.RANGER, "Bouncing Arrow", null, 3, 1);
    }

    @EventHandler
    public void playerShootArrowEvent(ProjectileLaunchEvent event) {
        if(!(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();

        if(!(arrow.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) arrow.getShooter();

        if(!isUser(player.getUniqueId())) {
            return;
        }

        arrow.setMetadata("bounce", new FixedMetadataValue(dc.getPlugin(), bounce.get(getSkillLevel(player.getUniqueId()) - 1)));
        arrow.setMetadata("blacklist", new FixedMetadataValue(dc.getPlugin(), Arrays.asList(player.getUniqueId())));
        arrow.setMetadata("distance", new FixedMetadataValue(dc.getPlugin(), distance.get(getSkillLevel(player.getUniqueId()) - 1)));
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();

        if(!arrow.hasMetadata("bounce")) {
            return;
        }

        int bounce = arrow.getMetadata("bounce").get(0).asInt();
        double distance = arrow.getMetadata("distance").get(0).asDouble();
        List<UUID> blacklist = new ArrayList<>();

        ((List<UUID>) arrow.getMetadata("blacklist").get(0).value()).forEach(uuid -> blacklist.add(uuid));

        if(blacklist.isEmpty()) {
            return;
        }

        if(bounce <= 0) {
            return;
        }

        blacklist.add(event.getEntity().getUniqueId());

        List<LivingEntity> nearby = event.getEntity().getNearbyEntities(distance, distance, distance).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> !blacklist.contains(entity.getUniqueId()))
                .collect(Collectors.toList());

        if(nearby.isEmpty()) {
            return;
        }

        LivingEntity closest = nearby.stream()
                .min((a, b) -> (int) (a.getLocation().distance(event.getEntity().getLocation()) - b.getLocation().distance(event.getEntity().getLocation())))
                .get();

        Location startingLocation = event.getEntity().getLocation().add(0, event.getEntity().getHeight(), 0);
        Vector direction = closest.getLocation().add(0, closest.getHeight(), 0).subtract(startingLocation).toVector().normalize();
        Projectile newArrow = event.getEntity().getWorld().spawnArrow(startingLocation.add(direction), direction, (float) arrow.getVelocity().length(), 1);

        newArrow.setShooter(arrow.getShooter());
        newArrow.setMetadata("bounce", new FixedMetadataValue(dc.getPlugin(), bounce - 1));
        newArrow.setMetadata("blacklist", new FixedMetadataValue(dc.getPlugin(), blacklist));
        newArrow.setMetadata("distance", new FixedMetadataValue(dc.getPlugin(), distance));

    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("Arrows bounce ").color(NamedTextColor.GRAY))
                .append(ComponentUtil.skillLevelValues(skillLevel, bounce, NamedTextColor.YELLOW)));
        lore.add(Component.text("times to the nearest enemy").color(NamedTextColor.GRAY));
        lore.add(Component.text("within ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, distance, NamedTextColor.YELLOW))
                .append(Component.text(" blocks").color(NamedTextColor.GRAY)));
        return lore;
    }
}
