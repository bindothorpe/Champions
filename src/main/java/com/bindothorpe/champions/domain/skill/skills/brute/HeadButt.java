package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.BlockUtil;
import com.bindothorpe.champions.util.ChatUtil;
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

public class HeadButt extends Skill implements ReloadableData {

    private static double COLLISION_RADIUS;
    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;
    private static double BASE_WALL_IMPACT_DAMAGE;
    private static double WALL_IMPACT_DAMAGE_INCREASE_PER_LEVEL;
    private static double BASE_LAUNCH_STRENGTH;
    private static double LAUNCH_STRENGTH_INCREASE_PER_LEVEL;
    private static double BASE_IMPACT_LAUNCH_STRENGTH;
    private static double IMPACT_LAUNCH_STRENGTH_INCREASE_PER_LEVEL;

    private final Set<UUID> active = new HashSet<>();
    private final Map<UUID, Location> startingLocations = new HashMap<>();

    private final Set<UUID> hitActive = new HashSet<>();
    private final Map<UUID, UUID> hitBy = new HashMap<>();
    private final Map<UUID, Location> hitStartingLocations = new HashMap<>();

    public HeadButt(DomainController dc) {
        super(dc, "Head Butt", SkillId.HEAD_BUTT, SkillType.AXE, ClassType.BRUTE);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if (!activate(event.getPlayer().getUniqueId(), event)) {
            return;
        }

        Player player = event.getPlayer();

        player.setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(calculateBasedOnLevel(BASE_LAUNCH_STRENGTH, LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player))));
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


            CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) hit, player, calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(uuid)), player.getLocation(), CustomDamageSource.SKILL, getName());

            //Get the direction of the player's headbutt by subtracting the starting location's vector from the current location's vector
            Vector dir = player.getLocation().toVector().subtract(startingLocation.toVector()).setY(0).normalize().setY(0.2).normalize();

            CustomDamageCommand damageCommand = new CustomDamageCommand(dc, damageEvent)
                    .direction(dir)
                    .force(calculateBasedOnLevel(BASE_IMPACT_LAUNCH_STRENGTH, IMPACT_LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(uuid)));

            damageEvent.setCommand(damageCommand);


            Bukkit.getPluginManager().callEvent(damageEvent);

            if(damageEvent.isCancelled()) {
                active.remove(uuid);
                startingLocations.remove(uuid);
                continue;
            }



            hit.teleport(hit.getLocation().add(0, 0.2, 0));
            damageCommand.execute();

            active.remove(uuid);
            startingLocations.remove(uuid);

            hitActive.add(hit.getUniqueId());
            hitBy.put(hit.getUniqueId(), player.getUniqueId());
            hitStartingLocations.put(hit.getUniqueId(), hit.getLocation());

            player.setVelocity(new Vector(0, 0, 0));

            ChatUtil.sendMessage(player, ChatUtil.Prefix.SKILL, Component.text("You hit ").color(NamedTextColor.GRAY)
                    .append(Component.text(hit.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" with ").color(NamedTextColor.GRAY))
                    .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" level ").color(NamedTextColor.GRAY))
                    .append(Component.text(getSkillLevel(uuid)).color(NamedTextColor.YELLOW)));

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

            CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) entity, (LivingEntity) Bukkit.getEntity(hitBy.get(uuid)), calculateBasedOnLevel(BASE_WALL_IMPACT_DAMAGE, WALL_IMPACT_DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(hitBy.get(uuid))), entity.getLocation(), CustomDamageSource.SKILL, getName());
            CustomDamageCommand damageCommand = new CustomDamageCommand(dc, damageEvent)
                    .direction(new Vector(0, 0, 0));
            damageEvent.setCommand(damageCommand);

            Bukkit.getPluginManager().callEvent(damageEvent);

            if(damageEvent.isCancelled()) {
                hitActive.remove(uuid);
                hitBy.remove(uuid);
                hitStartingLocations.remove(uuid);
                continue;

            }



            damageCommand.execute();

            dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.STUN, uuid, getNamespacedKey(uuid), 1, 0.5);
            hitActive.remove(uuid);
            hitBy.remove(uuid);
            hitStartingLocations.remove(uuid);

        }

        hitActive.removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            if (!BlockUtil.getNearbyBlocks(entity.getLocation(), 1, 0, 0.1, 0).isEmpty()) {
                hitStartingLocations.remove(uuid);
                hitBy.remove(uuid);
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
        lore.add(ComponentUtil.skillValuesBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)
                .append(Component.text(" damage").color(NamedTextColor.GRAY)));
        lore.add(Component.text(" "));
        lore.add(Component.text("If they get knocked into a").color(NamedTextColor.GRAY));
        lore.add(Component.text("wall, they will be stunned").color(NamedTextColor.GRAY));
        lore.add(Component.text("for 0.5 seconds and take").color(NamedTextColor.GRAY));
        lore.add(ComponentUtil.skillValuesBasedOnLevel(BASE_WALL_IMPACT_DAMAGE, WALL_IMPACT_DAMAGE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)
                .append(Component.text(" damage").color(NamedTextColor.GRAY)));
        return lore;
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.head_butt.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.head_butt.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.cooldown_reduction_per_level");
            COLLISION_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.collision_radius");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.damage_increase_per_level");
            BASE_WALL_IMPACT_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.base_wall_impact_damage");
            WALL_IMPACT_DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.wall_impact_damage_increase_per_level");
            BASE_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.base_launch_strength");
            LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.base_impact_launch_strength");
            BASE_IMPACT_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.base_impact_launch_strength");
            IMPACT_LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.brute.head_butt.impact_launch_strength_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}