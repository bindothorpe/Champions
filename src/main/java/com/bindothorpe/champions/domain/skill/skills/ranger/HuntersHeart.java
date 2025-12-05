package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ComponentUtil;
import jdk.jfr.EventType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class HuntersHeart extends Skill {

    private static final double HEALING = 1.0D;
    private final Set<UUID> active = new HashSet<>();
    private final Map<UUID, Integer> taskIdMap = new HashMap<>();
    private final List<Double> durationBeforeHeal = List.of(12D, 10D, 8D);
    private final List<Double> intervalBetweenHeal = List.of(2D, 1.5D, 1D);
    private static final double INTERVAL = 2.0;
    public HuntersHeart(DomainController dc) {
        super(dc, SkillId.HUNTERS_HEART, SkillType.PASSIVE_B, ClassType.RANGER, "Hunter's Heart", null, 3, 1);
    }


    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.RAPID))
            return;

        for (UUID uuid : getUsers()) {
            if(dc.getCombatLogger().hasTakenDamageWithinDuration(uuid, durationBeforeHeal.get(getSkillLevel(uuid) - 1))){
                if(!active.contains(uuid)) continue;

                active.remove(uuid);
                int taskId = taskIdMap.remove(uuid);
                dc.getPlugin().getServer().getScheduler().cancelTask(taskId);

            } else {
                if(active.contains(uuid)) continue;
                Player player = Bukkit.getPlayer(uuid);
                if(player == null) continue;;
                if(player.getHealth() == Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue()) continue;
                active.add(uuid);
                BukkitTask task =  dc.getPlugin().getServer().getScheduler().runTaskTimer(
                        dc.getPlugin(),
                        ()-> performHuntersHeart(uuid),
                        0L,
                        (long) (intervalBetweenHeal.get(getSkillLevel(uuid) - 1) * 20L)
                );
                taskIdMap.put(uuid, task.getTaskId());

            }
        }

    }

    private void performHuntersHeart(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        player.heal(HEALING);
        player.getWorld().spawnParticle(Particle.HEART, player.getEyeLocation(), 1);
        if(player.getHealth() == Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue()) {
            active.remove(uuid);
            int taskId = taskIdMap.remove(uuid);
            dc.getPlugin().getServer().getScheduler().cancelTask(taskId);
        }
//        player.setHealth(Math.min(player.getHealth() + healing.get(getSkillLevel(uuid) - 1), player.getMaxHealth()));
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                        .append(Component.text("Heal half a heart").color(NamedTextColor.GRAY)));
        lore.add(Component.text("every ").color(NamedTextColor.GRAY)
                    .append(ComponentUtil.skillLevelValues(skillLevel, intervalBetweenHeal, NamedTextColor.YELLOW)));
        lore.add(Component.text("seconds, after not taking").color(NamedTextColor.GRAY));
        lore.add(Component.text("damage for ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, durationBeforeHeal, NamedTextColor.YELLOW)));
        lore.add(Component.text("seconds.").color(NamedTextColor.GRAY));
        return lore;
    }
}
