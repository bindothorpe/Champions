package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStartBlockingEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStopBlockingEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerUpdateBlockingEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Riposte extends Skill implements ReloadableData {

    private static double BLOCK_WINDOW_DURATION;
    private static double BUFF_DURATION;
    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;
//
//    private final Map<UUID, Long> blockingUsersMap = new HashMap<>();
//    private final Set<UUID> blockedAttackUsersSet = new HashSet<>();

    private final Map<UUID, Long> activeBlockingUsersStartTimeMap = new HashMap<>();
    private final Map<UUID, Long> activeRipositeUsersStartTimeMap = new HashMap<>();


    public Riposte(DomainController dc) {
        super(dc, "Riposte", SkillId.RIPOSTE, SkillType.SWORD, ClassType.KNIGHT);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        activeRipositeUsersStartTimeMap.remove(uuid);
        activeBlockingUsersStartTimeMap.remove(uuid);
    }

    @EventHandler
    public void onStartBlocking(PlayerStartBlockingEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(!canUse(uuid, event).result()) return;

        startBlockingAttack(player);
    }

    @EventHandler
    public void onBlockCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getDamagee() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        if(!activeBlockingUsersStartTimeMap.containsKey(uuid)) return;

        // Block the attack and start the cooldown
        event.setCancelled(true);
        activeBlockingUsersStartTimeMap.remove(uuid);
        startRiposte(player);
        activate(uuid, event);
    }

    @EventHandler public void onRiposteCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getDamager() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        if(!activeRipositeUsersStartTimeMap.containsKey(uuid)) return;

        double additionalDamage = calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(event.getDamager().getUniqueId()));
        dc.getEntityStatusManager().addEntityStatus(player.getUniqueId(), new EntityStatus(
                EntityStatusType.ATTACK_DAMAGE_DONE,
                additionalDamage,
                0.2,
                false,
                false,
                this
        ));

        activeRipositeUsersStartTimeMap.remove(uuid);
        //TODO: Send success message
    }

    @EventHandler
    public void onUpdateBlocking(PlayerUpdateBlockingEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(activeBlockingUsersStartTimeMap.containsKey(uuid)) {
            // Check if the window has passed
            if(event.getBlockDuration() <= BLOCK_WINDOW_DURATION) return;

            //Fail blocking
            activeBlockingUsersStartTimeMap.remove(uuid);
            startCooldown(uuid);

            ChatUtil.sendMessage(
                    player,
                    ChatUtil.Prefix.SKILL,
                    Component.text("You failed to ").color(NamedTextColor.GRAY)
                            .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                            .append(Component.text(".").color(NamedTextColor.GRAY))
            );

        } else {
            // Try to start
            if(!canUse(uuid, event).result()) return;
            startBlockingAttack(player);
        }
    }

    @EventHandler
    public void onStopBlocking(PlayerStopBlockingEvent event) {
        Player player = event.getPlayer();
        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(!activeBlockingUsersStartTimeMap.containsKey(uuid)) return;

        //Fail blocking
        activeBlockingUsersStartTimeMap.remove(uuid);
        startCooldown(uuid);

        ChatUtil.sendMessage(
                player,
                ChatUtil.Prefix.SKILL,
                Component.text("You failed to ").color(NamedTextColor.GRAY)
                        .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.GRAY))
        );
    }

    @EventHandler
    public void expireRiposte(UpdateEvent updateEvent) {
        Set<UUID> expiredUsers = new HashSet<>();

        for(UUID uuid: getUsers()) {
            if(!activeRipositeUsersStartTimeMap.containsKey(uuid)) continue;

            if(getTimeElapsedSince(activeRipositeUsersStartTimeMap.get(uuid)) <= BUFF_DURATION) continue;

            expiredUsers.add(uuid);
        }

        expiredUsers
                .forEach((uuid) -> {
                    activeRipositeUsersStartTimeMap.remove(uuid);

                    Player player = Bukkit.getPlayer(uuid);
                    if(player == null) return;
                    ChatUtil.sendMessage(
                            player,
                            ChatUtil.Prefix.SKILL,
                            Component.text("You failed to ").color(NamedTextColor.GRAY)
                                    .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                                    .append(Component.text(".").color(NamedTextColor.GRAY))
                    );
                });
    }

    private void startBlockingAttack(@NotNull Player player) {
        if(activeBlockingUsersStartTimeMap.containsKey(player.getUniqueId())) return;
        activeBlockingUsersStartTimeMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void startRiposte(@NotNull Player player) {
        if(activeRipositeUsersStartTimeMap.containsKey(player.getUniqueId())) return;
        activeRipositeUsersStartTimeMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private double getTimeElapsedSince(long startTimeInMillis) {
        return (System.currentTimeMillis() - startTimeInMillis) * 1000;
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.knight.riposte.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.knight.riposte.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.riposte.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.riposte.cooldown_reduction_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.riposte.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.riposte.damage_increase_per_level");
            BLOCK_WINDOW_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.riposte.block_window_duration");
            BUFF_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.knight.riposte.buff_duration");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

}