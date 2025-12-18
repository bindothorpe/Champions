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
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Leap extends Skill implements ReloadableData {

    private static double BASE_LEAP_STRENGTH;
    private static double LEAP_STRENGTH_INCREASE_PER_LEVEL;
    private static double BASE_WALL_KICK_STRENGTH;
    private static double WALL_KICK_STRENGTH_INCREASE_PER_LEVEL;

    private final Set<UUID> wallKickSet = new HashSet<>();


    public Leap(DomainController dc) {
        super(dc, "Leap", SkillId.LEAP, SkillType.AXE, ClassType.ASSASSIN);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        wallKickSet.remove(uuid);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(!event.isAxe())
            return;

        if (canPerformWallKick(player)) {
            performWallKick(player);
        } else {
            performLeap(player, event);
        }

    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for (UUID uuid : getUsers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            if (wallKickSet.contains(uuid)) {
                if (((Entity) player).isOnGround()) {
                    wallKickSet.remove(uuid);
                }
            }
        }
    }

    private boolean canPerformWallKick(Player player) {
        if (!isUser(player.getUniqueId())) return false;

        Vector direction = player.getLocation().getDirection()
                .setY(0)
                .normalize()
                .multiply(-1);

        Block block = player.getLocation().clone().add(direction).getBlock();

        if (block.isPassable()) return false;

        return !wallKickSet.contains(player.getUniqueId());
    }

    private void performWallKick(Player player) {
        Vector direction = player.getLocation().getDirection();
        direction.setY(0);

        ChatUtil.sendSkillMessage(player, "Wall Kick", getSkillLevel(player.getUniqueId()));
        dc.getSoundManager().playSound(player, CustomSound.SKILL_LEAP);

        MobilityUtil.launch(player, direction, calculateBasedOnLevel(BASE_WALL_KICK_STRENGTH, WALL_KICK_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId())), false, 0.0, 0.7, 2.0, true);
        wallKickSet.add(player.getUniqueId());
    }

    private void performLeap(Player player, PlayerRightClickEvent event) {
        if (!activate(player.getUniqueId(), event)) {
            return;
        }

        dc.getSoundManager().playSound(player, CustomSound.SKILL_LEAP);
        MobilityUtil.launch(player, calculateBasedOnLevel(BASE_LEAP_STRENGTH, LEAP_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId())), 0.2, 1.0, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                wallKickSet.remove(player.getUniqueId());
            }
        }.runTaskLater(dc.getPlugin(), 10L);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();

        lore.add(ComponentUtil.active()
                .append(ComponentUtil.rightClick())
                .append(Component.text("to perform").color(NamedTextColor.GRAY)));
        lore.add(Component.text("a Leap, launching your self").color(NamedTextColor.GRAY));
        lore.add(Component.text("with a force of ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_LEAP_STRENGTH, LEAP_STRENGTH_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)));
        lore.add(Component.text("in the direction you are").color(NamedTextColor.GRAY));
        lore.add(Component.text("facing").color(NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(ComponentUtil.rightClick(true)
                .append(Component.text("while facing away").color(NamedTextColor.GRAY)));
        lore.add(Component.text("from a wall to perform a").color(NamedTextColor.GRAY));
        lore.add(Component.text("Wall Kick, launching yourself").color(NamedTextColor.GRAY));
        lore.add(Component.text("in the opposite direction with").color(NamedTextColor.GRAY));
        lore.add(Component.text("a force of ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_WALL_KICK_STRENGTH, WALL_KICK_STRENGTH_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)));

        return lore;
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.leap.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.leap.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.leap.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.leap.cooldown_reduction_per_level");
            BASE_LEAP_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.leap.base_leap_strength");
            LEAP_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.leap.leap_strength_increase_per_level");
            BASE_WALL_KICK_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.leap.base_wall_kick_strength");
            WALL_KICK_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.leap.wall_kick_strength_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

}
