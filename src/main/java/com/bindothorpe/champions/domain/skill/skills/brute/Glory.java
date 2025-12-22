package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.death.CustomDeathEvent;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.EntityUtil;
import jdk.jfr.Percentage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Glory extends Skill implements ReloadableData {


    private double STARTING_HEALTH_DECAY_PER_TICK;
    private double HEALTH_DECAY_INCREASE_PER_TICK;
    private @Percentage double BASE_LIFE_STEAL_PERCENTAGE;
    private @Percentage double LIFE_STEAL_PERCENTAGE_INCREASE_PER_LEVEL;

    private final Map<UUID, Double> activeDecayRate = new HashMap<>();

    public Glory(DomainController dc) {
        super(dc, "Glory", SkillId.GLORY, SkillType.PASSIVE_B, ClassType.BRUTE);
    }

    @EventHandler
    public void onCustomDeath(CustomDeathEvent event) {

        //If the player is currently in the glory state, remove it and kill it for real
        if(activeDecayRate.containsKey(event.getPlayer().getUniqueId())) {
            activeDecayRate.remove(event.getPlayer().getUniqueId());
            return;
        }

        event.setCancelled(true);

        EntityUtil.fullyHealEntity(event.getPlayer());
        dc.getStatusEffectManager().addStatusEffectToEntity(
                StatusEffectType.STASIS,
                event.getPlayer().getUniqueId(),
                getNamespacedKey(event.getPlayer()),
                1,
                1.5);
        new Timer(dc.getPlugin(), 1.5, () -> {
            activeDecayRate.put(event.getPlayer().getUniqueId(), STARTING_HEALTH_DECAY_PER_TICK);
        }).start();

    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        if(activeDecayRate.containsKey(event.getPlayer().getUniqueId()))
            event.setCancelled(true,
                    Component.text("You cannot use skills while ", NamedTextColor.GRAY)
                    .append(Component.text(getName(), NamedTextColor.YELLOW))
                    .append(Component.text(" is active.", NamedTextColor.GRAY)));
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.getDamager() == null) return;
        if(!activeDecayRate.containsKey(event.getDamager().getUniqueId())) return;
        if(!(event.getDamager() instanceof Player damager)) return;

        double damageToHeal = event.getDamage() * calculateBasedOnLevel(BASE_LIFE_STEAL_PERCENTAGE, LIFE_STEAL_PERCENTAGE_INCREASE_PER_LEVEL, getSkillLevel(damager));

        if(damageToHeal <= 0) return;
        damager.heal(damageToHeal);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.isTick()) return;

        for(UUID uuid : getUsers()) {
            if(!activeDecayRate.containsKey(uuid)) continue;

            Player player = Bukkit.getPlayer(uuid);
            if(player == null) continue;

            CustomDamageEvent customDamageEvent = CustomDamageEvent.getBuilder()
                    .setDamage(activeDecayRate.get(uuid))
                    .setDamagee(player)
                    .setCause(CustomDamageEvent.DamageCause.SKILL)
                    .setPlayDamageEffectAndSound(false)
                    .build();

            customDamageEvent.callEvent();
            if(customDamageEvent.isCancelled()) continue;

            activeDecayRate.put(uuid, activeDecayRate.get(uuid) + HEALTH_DECAY_INCREASE_PER_TICK);
            new CustomDamageCommand(dc, customDamageEvent).execute();

            spawnParticles(player);
        }
    }

    private void spawnParticles(@NotNull LivingEntity entity) {
        Location loc = entity.getLocation();

        double height = entity.getHeight();
        double radius = entity.getWidth() * 1.2;

        // Ambient sparkles
        for (int i = 0; i < 3; i++) {
            double randX = (Math.random() - 0.5) * radius * 2;
            double randY = Math.random() * height;
            double randZ = (Math.random() - 0.5) * radius * 2;

            Location location = loc.clone().add(randX, randY, randZ);
            loc.getWorld().spawnParticle(
                    Particle.TRIAL_OMEN,
                    location,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        activeDecayRate.remove(uuid);
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            STARTING_HEALTH_DECAY_PER_TICK = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("starting_health_decay_per_tick"));
            HEALTH_DECAY_INCREASE_PER_TICK = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("health_decay_increase_per_tick"));
            BASE_LIFE_STEAL_PERCENTAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_life_steal_percentage"));
            LIFE_STEAL_PERCENTAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("life_steal_percentage_increase_per_level"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.passive()
                        .append(Component.text("Upon taking lethal damage, become invulnerable for ", NamedTextColor.GRAY))
                        .append(Component.text("1.5", NamedTextColor.YELLOW))
                        .append(Component.text(" seconds, then enter a ", NamedTextColor.GRAY))
                        .append(Component.text("Glory", NamedTextColor.YELLOW))
                        .append(Component.text(" state where your health constantly decays.", NamedTextColor.GRAY)),
                33));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("While in ", NamedTextColor.GRAY)
                        .append(Component.text("Glory", NamedTextColor.YELLOW))
                        .append(Component.text(", you cannot use skills, but gain ", NamedTextColor.GRAY))
                        .append(ComponentUtil.skillValuesBasedOnLevel((int) (100 * BASE_LIFE_STEAL_PERCENTAGE), (int) (100 * LIFE_STEAL_PERCENTAGE_INCREASE_PER_LEVEL), skillLevel, MAX_LEVEL, true, NamedTextColor.YELLOW))
                        .append(Component.text(" lifesteal on all damage dealt.", NamedTextColor.GRAY)),
                33));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("If your health reaches zero during ", NamedTextColor.GRAY)
                        .append(Component.text("Glory", NamedTextColor.YELLOW))
                        .append(Component.text(", you die.", NamedTextColor.GRAY)),
                33));
        return lore;
    }
}
