package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.BlockUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class GrandEntrance extends Skill implements ReloadableData {

    private final Set<UUID> active = new HashSet<>();
    private final Set<UUID> active2 = new HashSet<>();

    protected  static double BASE_DAMAGE;
    protected  static double DAMAGE_INCREASE_PER_LEVEL;
    protected  static double BASE_LAUNCH_STRENGTH;
    protected  static double LAUNCH_STRENGTH_INCREASE_PER_LEVEL;
    protected  static double BASE_LAUNCH_DOWN_STRENGTH;
    protected  static double LAUNCH_DOWN_STRENGTH_INCREASE_PER_LEVEL;
    protected  static double BASE_KNOCK_UP_RADIUS;
    protected  static double KNOCK_UP_RADIUS_INCREASE_PER_LEVEL;
    protected  static double BASE_KNOCK_UP_STRENGTH;
    protected  static double KNOCK_UP_STRENGTH_INCREASE_PER_LEVEL;

    public GrandEntrance(DomainController dc) {
        super(dc, "Grand Entrance", SkillId.GRAND_ENTRANCE, SkillType.AXE, ClassType.BRUTE);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        active.remove(uuid);
        active2.remove(uuid);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(active.contains(event.getPlayer().getUniqueId())) {
            player.setVelocity(new Vector(0, -1, 0).multiply(calculateBasedOnLevel(BASE_LAUNCH_DOWN_STRENGTH, LAUNCH_DOWN_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player))));
            active.remove(player.getUniqueId());
            active2.add(player.getUniqueId());
            return;
        }

        boolean success = activate(event.getPlayer().getUniqueId(), event);

        if (!success) {
            return;
        }

        Vector dir = player.getLocation().getDirection().setY(0).normalize().setY(0.9).normalize();
        player.setVelocity(player.getVelocity().add(dir.multiply(calculateBasedOnLevel(BASE_LAUNCH_STRENGTH, LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player)))));

        new BukkitRunnable() {
            @Override
            public void run() {
                active.add(player.getUniqueId());
            }
        }.runTaskLater(dc.getPlugin(), 2L);

    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for(UUID uuid : active2) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null)
                continue;

            Set<Block> blocks = BlockUtil.getNearbyBlocks(player.getLocation(), 1, 0.1, 0.1, 0.1);

            if(blocks.isEmpty())
                continue;

            performStomp(player);
            active2.remove(uuid);
        }

        for(UUID uuid : active) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null)
                continue;

            Set<Block> blocks = BlockUtil.getNearbyBlocks(player.getLocation(), 1, 0.1, 0.1, 0.1);

            if(blocks.isEmpty())
                continue;

            active.remove(uuid);
        }
    }

    private void performStomp(Player player) {
        double radius = calculateBasedOnLevel(BASE_KNOCK_UP_RADIUS, KNOCK_UP_RADIUS_INCREASE_PER_LEVEL, getSkillLevel(player));
        Set<Entity> nearby = player.getLocation().getNearbyEntities(radius, 1, radius)
                .stream()
                .filter(entity -> !dc.getTeamManager().getTeamFromEntity(player).equals(dc.getTeamManager().getTeamFromEntity(entity)))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(Entity::isOnGround)
                .collect(Collectors.toSet());

        double damage = calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(player));
        Vector direction = new Vector(0, 1, 0);

        double knockUpStrength = calculateBasedOnLevel(BASE_KNOCK_UP_STRENGTH, KNOCK_UP_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player));
        for(Entity entity : nearby) {
            CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) entity, player, damage, player.getLocation(), CustomDamageSource.SKILL, getName());
            CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damageEvent).direction(direction).force(knockUpStrength);
            damageEvent.setCommand(customDamageCommand);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if(damageEvent.isCancelled())
                continue;

            customDamageCommand.execute();
        }
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerRightClickEvent e))
            return AttemptResult.FALSE;


        if (!e.isAxe())
            return AttemptResult.FALSE;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        return lore;
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.cooldown_reduction_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.damage_increase_per_level");
            BASE_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.base_launch_strength");
            LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.launch_strength_increase_per_level");
            BASE_LAUNCH_DOWN_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.base_launch_down_strength");
            LAUNCH_DOWN_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.launch_down_strength_increase_per_level");
            BASE_KNOCK_UP_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.base_knock_up_radius");
            KNOCK_UP_RADIUS_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.knock_up_radius_increase_per_level");
            BASE_KNOCK_UP_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.base_knock_up_strength");
            KNOCK_UP_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.grand_entrance.knock_up_strength_increase_per_level");

            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }



}
