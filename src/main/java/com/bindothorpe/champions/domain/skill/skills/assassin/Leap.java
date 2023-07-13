package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
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

public class Leap extends Skill {

    private final Set<UUID> wallKickSet = new HashSet<>();
    private final List<Double> leapStrength = List.of(1.0, 1.2, 1.4);
    private final List<Double> wallKickStrength = List.of(0.7, 0.9, 1.1);

    public Leap(DomainController dc) {
        super(dc, SkillId.LEAP, SkillType.AXE, ClassType.ASSASSIN, "Leap", List.of(5.0d, 4.0d, 3.0d), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

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

        MobilityUtil.launch(player, direction, wallKickStrength.get(getSkillLevel(player.getUniqueId()) - 1), false, 0.0, 0.7, 2.0, true);
        wallKickSet.add(player.getUniqueId());
    }

    private void performLeap(Player player, PlayerRightClickEvent event) {
        if (!activate(player.getUniqueId(), event)) {
            return;
        }

        dc.getSoundManager().playSound(player, CustomSound.SKILL_LEAP);
        MobilityUtil.launch(player, leapStrength.get(getSkillLevel(player.getUniqueId()) - 1), 0.2, 1.0, true);
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
                .append(ComponentUtil.skillLevelValues(skillLevel, leapStrength, NamedTextColor.YELLOW)));
        lore.add(Component.text("in the direction you are").color(NamedTextColor.GRAY));
        lore.add(Component.text("facing").color(NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(ComponentUtil.rightClick(true)
                .append(Component.text("while facing away").color(NamedTextColor.GRAY)));
        lore.add(Component.text("from a wall to perform a").color(NamedTextColor.GRAY));
        lore.add(Component.text("Wall Kick, launching yourself").color(NamedTextColor.GRAY));
        lore.add(Component.text("in the opposite direction with").color(NamedTextColor.GRAY));
        lore.add(Component.text("a force of ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, wallKickStrength, NamedTextColor.YELLOW)));

        return lore;
    }

}
