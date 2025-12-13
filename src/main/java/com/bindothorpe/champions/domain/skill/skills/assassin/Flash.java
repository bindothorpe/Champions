package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import com.bindothorpe.champions.util.actionBar.ActionBarPriority;
import com.bindothorpe.champions.util.actionBar.ActionBarUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Flash extends Skill implements ReloadableData {

    private double DISTANCE;
    private int BASE_CHARGE_COUNT;
    private int CHARGE_COUNT_INCREASE_PER_LEVEL;

    private final Map<UUID, Integer> flashCharges = new HashMap<>();
    private final Map<UUID, Timer> timerMap = new HashMap<>();

    public Flash(DomainController dc) {
        super(dc, "Flash", SkillId.FLASH, SkillType.AXE, ClassType.ASSASSIN);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!activate(uuid, event, false)) return;

        flashCharges.put(uuid, flashCharges.get(uuid) - 1);
        double currentDistance = 0;

        Location startingLocation = player.getLocation();
        Location targetLocation = player.getLocation();

        while(currentDistance <= DISTANCE) {
            Location location = startingLocation.clone().add(0, 0.2, 0).add(player.getLocation().getDirection().multiply(currentDistance));

            //Exit the loop
            if(!location.getBlock().isPassable() || !location.getBlock().getRelative(BlockFace.UP).isPassable()) break;

            currentDistance += 1.0;
            targetLocation = location;
        }

        player.teleport(targetLocation);
        player.setFallDistance(0);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_FLASH);
        spawnParticleLine(startingLocation.clone().add(0, 0.5, 0), player.getLocation().clone().add(0, 0.5, 0));


    }

    private void spawnParticleLine(@NotNull Location startLocation, @NotNull Location endLocation) {
        if(startLocation.getWorld() != endLocation.getWorld()) return;

        World world = startLocation.getWorld();

        Set<org.bukkit.util.Vector> points = ShapeUtil.line(startLocation.toVector(), endLocation.toVector(), 0.2);

        for(Vector point : points) {
            startLocation.getWorld().spawnParticle(
                    Particle.FIREWORK,
                    new Location(world, point.getX(), point.getY(), point.getZ()),
                    1,      // spawn 3 particles for a thicker cloud
                    0.0,    // slight X spread
                    0.0,    // slight Y spread
                    0.0,    // slight Z spread
                    0.0     // no velocity
            );
        }
    }



    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerRightClickEvent rightClickEvent)) return false;

        if(!rightClickEvent.isAxe()) return false;

        flashCharges.computeIfAbsent(uuid, k -> calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(uuid)));

        if(flashCharges.get(uuid) == 0) {
            ChatUtil.sendMessage(rightClickEvent.getPlayer(), ChatUtil.Prefix.COOLDOWN, Component.text("You cannot use ").color(NamedTextColor.GRAY)
                    .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" for ").color(NamedTextColor.GRAY))
                    .append(Component.text(String.format(Locale.US, "%.1f", timerMap.get(uuid).getTimeLeftInSeconds())).color(NamedTextColor.YELLOW))
                    .append(Component.text(" seconds").color(NamedTextColor.GRAY)));
            return false;
        }

        return super.canUseHook(uuid, event);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {

        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        for(UUID uuid : getUsers()) {
            flashCharges.computeIfAbsent(uuid, k -> calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(uuid)));

            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            // Start cooldown logic

            if(flashCharges.get(uuid) < calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(player))) {
                if(timerMap.get(uuid) == null || !timerMap.get(uuid).isRunning()) {
                    Timer timer = new Timer(
                            dc.getPlugin(),
                            calculateBasedOnLevel(BASE_COOLDOWN, COOLDOWN_REDUCTION_PER_LEVEL, getSkillLevel(player)),
                            () -> {
                                flashCharges.put(uuid, flashCharges.get(uuid) + 1);
                                timerMap.remove(uuid);
                                dc.getSoundManager().playSound(player, CustomSound.SKILL_COOLDOWN_END);
                            }
                    );
                    timer.start();
                    timerMap.put(uuid, timer);
                }
            }

            if(!isItemStackOfSkillType(player.getInventory().getItemInMainHand())) continue;

//            double percentage = timerMap.containsKey(uuid) ? timerMap.get(uuid).getPercentage() : -1;

            ActionBarUtil.sendMessage(
                    player,
                    ComponentUtil.skillCharges(null, flashCharges.get(player.getUniqueId()), calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(player)), -1),
                    ActionBarPriority.HIGH
            );

        }

    }

    @EventHandler
    public void onPlayerLeaveCurrentItemSlotForCharges(PlayerItemHeldEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;
        if(!canDisplayOnSkillType()) return;

        ItemStack previousItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());

        if(!isItemStackOfSkillType(previousItem)) return;

        clearActionBar(event.getPlayer());
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("cooldown_reduction_per_level"));
            DISTANCE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("distance"));
            BASE_CHARGE_COUNT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("base_charge_count"));
            CHARGE_COUNT_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("charge_count_increase_per_level"));
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
