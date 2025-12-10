package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
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

public class HuntersHeart extends Skill implements ReloadableData {

    private final Set<UUID> active = new HashSet<>();
    private final Map<UUID, Integer> taskIdMap = new HashMap<>();

    private static double HEAL_AMOUNT;
    private static double BASE_DURATION_BEFORE_HEAL;
    private static double DURATION_BEFORE_HEAL_DECREASE_PER_LEVEL;
    private static double BASE_INTERVAL_BETWEEN_HEAL;
    private static double INTERVAL_BETWEEN_HEAL_DECREASE_PER_LEVEL;

    public HuntersHeart(DomainController dc) {
        super(dc, "Hunter's Heart", SkillId.HUNTERS_HEART, SkillType.PASSIVE_B, ClassType.RANGER);
    }


    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.RAPID))
            return;

        for (UUID uuid : getUsers()) {
            if(dc.getCombatLogger().hasTakenDamageWithinDuration(uuid, calculateBasedOnLevel(BASE_DURATION_BEFORE_HEAL, -DURATION_BEFORE_HEAL_DECREASE_PER_LEVEL, getSkillLevel(uuid)))){
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
                        (long) (calculateBasedOnLevel(BASE_INTERVAL_BETWEEN_HEAL, -INTERVAL_BETWEEN_HEAL_DECREASE_PER_LEVEL, getSkillLevel(uuid)) * 20L)
                );
                taskIdMap.put(uuid, task.getTaskId());

            }
        }

    }

    private void performHuntersHeart(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        player.heal(HEAL_AMOUNT);
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
                    .append(ComponentUtil.skillValuesBasedOnLevel(BASE_INTERVAL_BETWEEN_HEAL, -INTERVAL_BETWEEN_HEAL_DECREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)));
        lore.add(Component.text("seconds, after not taking").color(NamedTextColor.GRAY));
        lore.add(Component.text("damage for ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DURATION_BEFORE_HEAL, -DURATION_BEFORE_HEAL_DECREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)));
        lore.add(Component.text("seconds.").color(NamedTextColor.GRAY));
        return lore;
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.hunters_heart.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.hunters_heart.level_up_cost");
            HEAL_AMOUNT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.ranger.hunters_heart.heal_amount");
            BASE_DURATION_BEFORE_HEAL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.hunters_heart.base_duration_before_heal");
            DURATION_BEFORE_HEAL_DECREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.hunters_heart.duration_before_heal_decrease_per_level");
            BASE_INTERVAL_BETWEEN_HEAL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.hunters_heart.base_interval_between_heal");
            INTERVAL_BETWEEN_HEAL_DECREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.ranger.hunters_heart.interval_between_heal_decrease_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
