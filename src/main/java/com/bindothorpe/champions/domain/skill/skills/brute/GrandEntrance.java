package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
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

public class GrandEntrance extends Skill {

    private final Set<UUID> active = new HashSet<>();
    private final Set<UUID> active2 = new HashSet<>();
    private final List<Double> damage = List.of(1.0, 1.5, 2.0);
    private static final double DOWNWARDS_SPEED = 4;
    private static final double KNOCKUP_SPEED = 1;
    private static final double LAUNCH_SPEED = 1.5;

    public GrandEntrance(DomainController dc) {
        super(dc, SkillId.GRAND_ENTRANCE, SkillType.AXE, ClassType.BRUTE, "Grand Entrance", Arrays.asList(10.0, 7.0, 3.5), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(active.contains(event.getPlayer().getUniqueId())) {
            player.setVelocity(new Vector(0, -1, 0).multiply(DOWNWARDS_SPEED));
            active.remove(player.getUniqueId());
            active2.add(player.getUniqueId());
            return;
        }

        boolean success = activate(event.getPlayer().getUniqueId(), event);

        if (!success) {
            return;
        }

        Vector dir = player.getLocation().getDirection().setY(0).normalize().setY(0.9).normalize();
        player.setVelocity(player.getVelocity().add(dir.multiply(LAUNCH_SPEED)));

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
        Set<Entity> nearby = player.getLocation().getNearbyEntities(3, 1, 3)
                .stream()
                .filter(entity -> !dc.getTeamManager().getTeamFromEntity(player).equals(dc.getTeamManager().getTeamFromEntity(entity)))
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(Entity::isOnGround)
                .collect(Collectors.toSet());

        double damage = this.damage.get(getSkillLevel(player.getUniqueId()) - 1);
        Vector direction = new Vector(0, 1, 0);

        for(Entity entity : nearby) {
            CustomDamageEvent damageEvent = new CustomDamageEvent(dc, (LivingEntity) entity, player, damage, player.getLocation(), CustomDamageSource.SKILL, getName());
            CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, damageEvent).direction(direction).force(KNOCKUP_SPEED);
            damageEvent.setCommand(customDamageCommand);
            Bukkit.getPluginManager().callEvent(damageEvent);

            if(damageEvent.isCancelled())
                continue;

            customDamageCommand.execute();
        }
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerRightClickEvent))
            return false;

        PlayerRightClickEvent e = (PlayerRightClickEvent) event;


        if (!e.isAxe())
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        return lore;
    }
}
