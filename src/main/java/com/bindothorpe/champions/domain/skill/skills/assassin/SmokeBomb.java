package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.interact.PlayerDropItemWrapperEvent;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SmokeBomb extends Skill implements ReloadableData {

    private final Map<UUID, Timer> activeInvisiblePlayersMap = new HashMap<>();

    private double BASE_INVISIBLE_DURATION;
    private double INVISIBLE_DURATION_INCREASE_PER_LEVEL;
    private double BLIND_RADIUS;
    private double BASE_BLIND_DURATION;
    private double BLIND_DURATION_INCREASE_PER_LEVEL;

    public SmokeBomb(DomainController dc) {
        super(dc, "Smoke Bomb", SkillId.SMOKE_BOMB, SkillType.PASSIVE_A, ClassType.ASSASSIN);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemWrapperEvent event) {
        if(event.getPlayer() == null) return;

        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        performSmokeBomb(event.getPlayer());
    }

    private void performSmokeBomb(@NotNull Player player) {
        double invisibleDuration = calculateBasedOnLevel(BASE_INVISIBLE_DURATION, INVISIBLE_DURATION_INCREASE_PER_LEVEL, getSkillLevel(player));

        dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.TRUE_INVISIBLE, player.getUniqueId(), getNamespacedKey(player), 1, invisibleDuration);
        Timer timer = new Timer(dc.getPlugin(), invisibleDuration, () ->
            activeInvisiblePlayersMap.remove(player.getUniqueId())

        );

        activeInvisiblePlayersMap.put(player.getUniqueId(), timer);
        timer.start();

        double blindDuration = calculateBasedOnLevel(BASE_BLIND_DURATION, BLIND_DURATION_INCREASE_PER_LEVEL, getSkillLevel(player));

        player.getLocation().getNearbyPlayers(BLIND_RADIUS).stream().
                filter((otherPlayer) -> dc.getTeamManager().areEntitiesOnDifferentTeams(player, otherPlayer))
                .forEach((otherPlayer) -> dc.getStatusEffectManager().addStatusEffectToEntity(StatusEffectType.BLIND, otherPlayer.getUniqueId(), getNamespacedKey(player), 1, blindDuration));

        spawnSmokeParticles(player);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_SMOKE_BOMB_ACTIVATE);
    }

    @EventHandler
    public void onCustomDamageEvent(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!(event.getDamager() instanceof Player player)) return;

        if(!activeInvisiblePlayersMap.containsKey(player.getUniqueId())) return;

        dc.getStatusEffectManager().removeStatusEffectFromPlayer(StatusEffectType.TRUE_INVISIBLE, player.getUniqueId(), getNamespacedKey(player));
        activeInvisiblePlayersMap.remove(player.getUniqueId()).stop();
    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        if(event.isCancelled()) return;

        Player player = event.getPlayer();

        if(player == null) return;

        UUID uuid = player.getUniqueId();

        if(!activeInvisiblePlayersMap.containsKey(uuid)) return;

        dc.getStatusEffectManager().removeStatusEffectFromPlayer(StatusEffectType.TRUE_INVISIBLE, uuid, getNamespacedKey(player));
        activeInvisiblePlayersMap.remove(uuid).stop();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.SECOND)) return;

        for(UUID uuid: activeInvisiblePlayersMap.keySet()) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 0.2, 0), 1, 0, 0, 0, 0);
        }
    }


    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {

        if(!(event instanceof PlayerDropItemWrapperEvent playerDropEvent)) return AttemptResult.FALSE;

        if(playerDropEvent.getPlayer() == null) return AttemptResult.FALSE;

        if(!playerDropEvent.isWeapon()) return AttemptResult.FALSE;

        if(playerDropEvent.getPlayer().isInWater()){
            return new AttemptResult(
                    false,
                    Component.text("You cannot use ", NamedTextColor.GRAY)
                            .append(Component.text(getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" while in water.", NamedTextColor.GRAY)),
                    ChatUtil.Prefix.SKILL
            );
        }

        return super.canUseHook(uuid, event);
    }

    private void spawnSmokeParticles(@NotNull Player player) {
        Location center = player.getLocation().clone().add(0, 1, 0);
        World world = center.getWorld();

        // Create a sphere of smoke particles
        int particleCount = (int) (BLIND_RADIUS * 100); // Scale particles with radius

        for (int i = 0; i < particleCount; i++) {
            // Generate random point in sphere
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.random() * Math.PI;
            double r = Math.random() * BLIND_RADIUS;

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.sin(phi) * Math.sin(theta);
            double z = r * Math.cos(phi);

            Location particleLocation = center.clone().add(x, y, z);

            // Spawn explosion particles for smoke effect
            world.spawnParticle(Particle.EXPLOSION, particleLocation, 1, 0, 0, 0, 0);
        }

        // Add some cloud particles for extra effect
        world.spawnParticle(Particle.CLOUD, center, (int)(BLIND_RADIUS * 50),
                BLIND_RADIUS * 0.5, BLIND_RADIUS * 0.5, BLIND_RADIUS * 0.5, 0.02);
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_bomb.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.smoke_bomb.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.smoke_bomb.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.smoke_bomb.cooldown_reduction_per_level");
            BLIND_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("blind_radius"));
            BASE_BLIND_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_blind_duration"));
            BLIND_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("blind_duration_increase_per_level"));
            BASE_INVISIBLE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_invisible_duration"));
            INVISIBLE_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("invisible_duration_increase_per_level"));
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
