package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
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
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.*;

public class Evade extends Skill implements ReloadableData {

    private static double COOLDOWN_ON_SUCCESS;
    private static double ACTIVE_DURATION;

    private final Map<UUID, Long> blockingUsersMap = new HashMap<>();

    public Evade(DomainController dc) {
        super(dc, "Evade", SkillId.EVADE, SkillType.SWORD, ClassType.ASSASSIN);
    }


    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(!event.isSword())
            return;

        if(blockingUsersMap.containsKey(player.getUniqueId()))
            return;

        if (!activate(player.getUniqueId(), event, false))
            return;


        //Add the user to the map
        blockingUsersMap.put(player.getUniqueId(), System.currentTimeMillis());

        //TODO: REMOVE THIS LINE:
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().clone().add(0, 0.5, 0), 1);

    }


    @EventHandler(priority = EventPriority.LOW)
    public void onCustomDamageBlock(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;

        if(event.getDamagee() == null) return;

        if(!(event.getDamagee() instanceof Player damagee)) return;

        if(!damagee.isBlocking()) return;

        if(!blockingUsersMap.containsKey(damagee.getUniqueId())) return;

        if(event.getDamager() == null) return;

        event.setCancelled(true);
        performEvade(damagee, event.getDamager());
    }

    private void performEvade(Player player, Entity damager) {
        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.TRUE_INVISIBLE, player.getUniqueId(), getNamespacedKey(player), 1, 0.3);

        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().clone().add(0, 0.5, 0), 1);
        if(!player.isSneaking()) {
            player.teleport(getLocationBehindEntity(damager));
        } else {
            player.teleport(getLocationInFrontEntity(damager, player.getLocation().getDirection()));
        }

        ChatUtil.sendMessage(
                player,
                ChatUtil.Prefix.SKILL,
                Component.text("You evaded ").color(NamedTextColor.GRAY)
                        .append(Component.text(damager.getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text("'s attack.").color(NamedTextColor.GRAY))
        );
        startCooldown(player.getUniqueId(), COOLDOWN_ON_SUCCESS);
    }

    private Location getLocationBehindEntity(Entity entity) {
        Location location = entity.getLocation().clone();

        location.add(entity.getLocation().getDirection().multiply(-1));
        location.setY(entity.getLocation().getY() + 0.1);

        //TODO: Check if it is a valid location

        return location;
    }

    private Location getLocationInFrontEntity(Entity entity, Vector facingDirection) {
        Location location = entity.getLocation().clone();

        location.add(entity.getLocation().getDirection());
        location.setY(entity.getLocation().getY() + 0.1);
        location.setDirection(facingDirection);

        //TODO: Check if it is a valid location

        return location;
    }

    @EventHandler
    public void onExpire(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        Set<UUID> expiredUsersSet = new HashSet<>();

        blockingUsersMap.entrySet().stream()
                .filter((entry) -> System.currentTimeMillis() > entry.getValue() + ((long) ACTIVE_DURATION * 1000L))
                .forEach((entry) -> expiredUsersSet.add(entry.getKey()));

        expiredUsersSet
                .forEach((uuid) -> {
                    blockingUsersMap.remove(uuid);
                    //Send a fail message
                    Player player = Bukkit.getPlayer(uuid);
                    if(player == null) return;
                    ChatUtil.sendMessage(
                            player,
                            ChatUtil.Prefix.SKILL,
                            Component.text("You failed to ").color(NamedTextColor.GRAY)
                                    .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                                    .append(Component.text(".").color(NamedTextColor.GRAY))
                    );
                    startCooldown(player.getUniqueId());
                });

    }

    @EventHandler
    public void onStopBlocking(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        for(UUID uuid: getUsers()) {
            if(!blockingUsersMap.containsKey(uuid)) continue;

            if(System.currentTimeMillis() - blockingUsersMap.get(uuid) < 250L) continue;

            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            if(player.isBlocking()) continue;

            blockingUsersMap.remove(uuid);
            ChatUtil.sendMessage(
                    player,
                    ChatUtil.Prefix.SKILL,
                    Component.text("You failed to ").color(NamedTextColor.GRAY)
                            .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                            .append(Component.text(".").color(NamedTextColor.GRAY))
            );
            startCooldown(player.getUniqueId());
        }

    }


    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.cooldown_reduction_per_level");
            COOLDOWN_ON_SUCCESS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.cooldown_on_success");
            ACTIVE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.evade.active_duration");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
