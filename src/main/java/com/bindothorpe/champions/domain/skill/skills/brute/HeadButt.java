package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.BlockUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class HeadButt extends Skill {

    private static final double COLLISION_RADIUS = 0.5;
    private static final double VELOCITY = 1.5;
    private final List<Double> damage = Arrays.asList(3.0, 4.0, 5.0);

    private final Set<UUID> active = new HashSet<>();
    private final Map<UUID, Location> startingLocations = new HashMap<>();

    private final Set<UUID> hitActive = new HashSet<>();
    private final Map<UUID, Location> hitStartingLocations = new HashMap<>();

    public HeadButt(DomainController dc) {
        super(dc, SkillId.HEAD_BUTT, SkillType.AXE, ClassType.BRUTE, "Head Butt", Arrays.asList(10.0, 7.5, 3.0), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if (!activate(event.getPlayer().getUniqueId(), event)) {
            return;
        }

        Player player = event.getPlayer();

        player.setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(VELOCITY));
        active.add(player.getUniqueId());
        startingLocations.put(player.getUniqueId(), player.getLocation());

    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for (UUID uuid : active) {
            Set<Entity> nearby = new HashSet<>();
            Player player = Bukkit.getPlayer(uuid);

            for (Entity entity : player.getLocation().clone().add(0, 0.5, 0).getNearbyEntities(COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS)) {
                if (!(entity instanceof LivingEntity))
                    continue;

                if(entity.equals(player))
                    continue;

                nearby.add(entity);

            }

            if(nearby.isEmpty())
                continue;

            Entity hit = nearby.stream().findFirst().get();
            Location startingLocation = startingLocations.get(uuid);

            //Get the direction of the player's headbutt by subtracting the starting location's vector from the current location's vector
            Vector dir = player.getLocation().toVector().subtract(startingLocation.toVector()).setY(0).normalize().setY(0.2).normalize();

            hit.teleport(hit.getLocation().add(0, 0.2, 0));
            hit.setVelocity(dir.multiply(VELOCITY));

            active.remove(uuid);
            startingLocations.remove(uuid);

            hitActive.add(hit.getUniqueId());
            hitStartingLocations.put(hit.getUniqueId(), hit.getLocation());

            player.sendMessage(Component.text("You hit ").color(NamedTextColor.GRAY)
                    .append(Component.text(hit.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" with ").color(NamedTextColor.GRAY))
                    .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" level ").color(NamedTextColor.GRAY))
                    .append(Component.text(getSkillLevel(uuid)).color(NamedTextColor.YELLOW)));

            player.setVelocity(new Vector(0, 0, 0));
        }

        active.removeIf(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player.isOnGround()) {
                startingLocations.remove(uuid);
                return true;
            }
            return false;
        });

        for(UUID uuid : hitActive) {
            Entity entity = Bukkit.getEntity(uuid);

            Set<Block> nearbyBlocks = BlockUtil.getNearbyBlocks(entity.getLocation().add(0, entity.getHeight()/2, 0), 0.2, entity.getWidth() / 2, 0.2, entity.getWidth() / 2).stream().filter(block -> !block.getType().isAir()).collect(Collectors.toSet());

            if(nearbyBlocks.isEmpty())
                continue;

            dc.addStatusEffectToEntity(StatusEffectType.STUN, uuid, 0.5);
            hitActive.remove(uuid);
            hitStartingLocations.remove(uuid);

        }

        hitActive.removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            if (!BlockUtil.getNearbyBlocks(entity.getLocation(), 1, 0, 0.1, 0).isEmpty()) {
                hitStartingLocations.remove(uuid);
                return true;
            }
            return false;
        });
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerRightClickEvent))
            return false;

        PlayerRightClickEvent playerRightClickEvent = (PlayerRightClickEvent) event;

        if (!playerRightClickEvent.isAxe())
            return false;

        if (playerRightClickEvent.getPlayer().isOnGround())
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();

        lore.add(ComponentUtil.active()
                .append(ComponentUtil.rightClick())
                .append(Component.text("to charge").color(NamedTextColor.GRAY)));
        lore.add(Component.text("forward and headbutt the").color(NamedTextColor.GRAY));
        lore.add(Component.text("first enemy you hit, knocking").color(NamedTextColor.GRAY));
        lore.add(Component.text("them back and dealing").color(NamedTextColor.GRAY));
        lore.add(ComponentUtil.skillLevelValues(skillLevel, damage, NamedTextColor.YELLOW)
                .append(Component.text(" damage").color(NamedTextColor.GRAY)));
        lore.add(Component.text(" "));
        lore.add(Component.text("If they get knocked into a").color(NamedTextColor.GRAY));
        lore.add(Component.text("wall, they will be stunned").color(NamedTextColor.GRAY));
        lore.add(Component.text("for 0.5 seconds and take").color(NamedTextColor.GRAY));
        lore.add(ComponentUtil.skillLevelValues(skillLevel, damage, NamedTextColor.YELLOW)
                .append(Component.text(" damage").color(NamedTextColor.GRAY)));
        return lore;
    }
}
