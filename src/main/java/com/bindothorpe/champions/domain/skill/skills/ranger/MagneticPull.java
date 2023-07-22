package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ItemUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MagneticPull extends Skill {

    private final Map<UUID, Set<Arrow>> playerArrowsMap = new HashMap<>();
    private final Map<UUID, Set<Arrow>> playerFollowingArrowsMap = new HashMap<>();
    private final Map<UUID, BukkitTask> arrowTaskMap = new HashMap<>();
    public MagneticPull(DomainController dc) {
        super(dc, SkillId.MAGNETIC_PULL, SkillType.PASSIVE_A, ClassType.RANGER, "Magnetic Pull", List.of(10d, 7.5d, 5d), 3, 1);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        //If it is not an arrow, return
        if(!(event.getEntity() instanceof Arrow))
            return;


        handleBlockHit(event);
        handleEntityHit(event);

    }

    private void handleEntityHit(ProjectileHitEvent event) {
        Arrow arrow = (Arrow) event.getEntity();

        Entity entityHit = event.getHitEntity();

        if(entityHit == null)
            return;

        if(!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        if(playerFollowingArrowsMap.get(player.getUniqueId()) == null)
            return;

        if(!playerFollowingArrowsMap.get(player.getUniqueId()).contains(arrow))
            return;

        if(entityHit.equals(player)) {
            event.setCancelled(true);
            playerFollowingArrowsMap.get(player.getUniqueId()).remove(arrow);
            arrow.remove();
            return;
        }

        playerFollowingArrowsMap.get(player.getUniqueId()).remove(arrow);
    }

    private void handleBlockHit(ProjectileHitEvent event) {

        Arrow arrow = (Arrow) event.getEntity();

        //If the shooter is not a player, return
        if(!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        //If the player is not a user, return
        if(!isUser(player.getUniqueId()))
            return;

        //If there is no block hit, return
        if(event.getHitBlock() == null)
            return;

        if(playerFollowingArrowsMap.get(player.getUniqueId()) != null && playerFollowingArrowsMap.get(player.getUniqueId()).contains(arrow)) {
            playerFollowingArrowsMap.get(player.getUniqueId()).remove(arrow);
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        //If the player is not on the arrow map, add him to it
        if(!playerArrowsMap.containsKey(player.getUniqueId()))
            playerArrowsMap.put(player.getUniqueId(), new HashSet<>());

        //Add the arrow to the player's set
        playerArrowsMap.get(player.getUniqueId()).add(arrow);

        //Add a task to remove it from the list in 10 seconds
        arrowTaskMap.put(arrow.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                arrowTaskMap.remove(arrow.getUniqueId());
            }
        }.runTaskLater(dc.getPlugin(), 20 * 20L));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        //Activate the skill
        boolean result = activate(event.getPlayer().getUniqueId(), event);

        //If the skill was not activated, return
        if(!result)
            return;

        //Cancel the event so the player does not drop the item
        event.setCancelled(true);

        //Get the player
        Player player = event.getPlayer();

        //Loop through all the arrows of the player
        for(Arrow arrow : new HashSet<>(playerArrowsMap.getOrDefault(player.getUniqueId(), new HashSet<>()))) {

            //If the arrow task map contains the key, cancel the task and remove it
            if(arrowTaskMap.containsKey(arrow.getUniqueId())) {
                arrowTaskMap.get(arrow.getUniqueId()).cancel();
                arrowTaskMap.remove(arrow.getUniqueId());
            }

            //Calculate the direction the arrow has to fly in
            Vector direction = calculateDirection(player, arrow);

            //Spawn a new arrow at the location of the old arrow, but put it a bit closer to the player, so it does not get stuck in a block
            Arrow newArrow = player.getWorld().spawnArrow(arrow.getLocation().add(direction.clone().multiply(0.5)), direction, 5F, 1);
            newArrow.setShooter(player);

            //Remove the old arrow
            arrow.remove();

            //If the player does not have a set of following arrows, create one
            if(!playerFollowingArrowsMap.containsKey(player.getUniqueId()))
                playerFollowingArrowsMap.put(player.getUniqueId(), new HashSet<>());

            //Add the new arrow to the set
            playerFollowingArrowsMap.get(player.getUniqueId()).add(newArrow);
            playerArrowsMap.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        if(!getAllArrows().contains(event.getArrow())) {
            return;
        }

        event.setCancelled(true);
    }

    private Collection<Arrow> getAllArrows() {
        return playerArrowsMap.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }


    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for(UUID uuid : playerFollowingArrowsMap.keySet()) {
            Player player = dc.getPlugin().getServer().getPlayer(uuid);
            if(player == null)
                continue;

            for(Arrow arrow : new HashSet<>(playerFollowingArrowsMap.get(uuid))) {
                if(!arrow.getLocation().getBlock().isPassable()) {
                    arrow.remove();
                    playerFollowingArrowsMap.get(uuid).remove(arrow);
                    continue;
                }
                Vector direction = calculateDirection(player, arrow);
                arrow.setVelocity(direction.clone().multiply(2));
            }
        }
    }

    private Vector calculateDirection(Player player, Projectile projectile) {
        return player.getEyeLocation().toVector().subtract(projectile.getLocation().toVector()).normalize();
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerDropItemEvent))
            return false;

        PlayerDropItemEvent dropItemEvent = (PlayerDropItemEvent) event;
        ItemStack item = dropItemEvent.getItemDrop().getItemStack();

        if(!ItemUtil.isWeapon(item))
            return false;

        if(playerArrowsMap.get(uuid) == null || playerArrowsMap.get(uuid).isEmpty())
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return new ArrayList<>();
    }
}
