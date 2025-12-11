package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerDropItemWrapperEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.*;

public class Recall extends Skill implements ReloadableData {

    private double LOCATION_DURATION;
    private double BASE_HEAL_AMOUNT;
    private double HEAL_AMOUNT_INCREASE_PER_LEVEL;

    private final Map<UUID, Deque<Location>> recallLocationMap = new HashMap<>();

    public Recall(DomainController dc) {
        super(dc, "Recall", SkillId.RECALL, SkillType.PASSIVE_A, ClassType.ASSASSIN);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemWrapperEvent event) {
        if(event.getPlayer() == null) return;

        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        performRecall(event.getPlayer());
    }

    @EventHandler
    public void storeLocationOnUpdate(UpdateEvent event) {
        if(!(event.getUpdateType().equals(UpdateType.HALF_SECOND))) return;

        for(UUID uuid: getUsers()) {
            Player player = Bukkit.getPlayer(uuid);

            // If the player is null, continue to the next UUID
            if(player == null) continue;

            recallLocationMap.computeIfAbsent(uuid, k -> new ArrayDeque<>());
            // Remember the location of the player
            recallLocationMap.get(uuid).add(player.getLocation());

            if(recallLocationMap.get(uuid).size() > (int) (LOCATION_DURATION / 0.5)) {
                recallLocationMap.get(uuid).pop();
            }
        }
    }


    @Override
    protected boolean canUseHook(UUID uuid, Event event) {

        if(!(event instanceof PlayerDropItemWrapperEvent playerDropEvent)) return false;

        if(playerDropEvent.getPlayer() == null) return false;

        if(!playerDropEvent.isWeapon()) return false;

        if(playerDropEvent.getPlayer().isInWater()){
            //TODO: Optionally let the player know they cant use it in water
            return false;
        }

        return super.canUseHook(uuid, event);
    }

    private void performRecall(Player player) {
        if(recallLocationMap.get(player.getUniqueId()) == null) return;

        Location location = recallLocationMap.get(player.getUniqueId()).getFirst();

        if(location == null) return;

        for(Location locationTrail : recallLocationMap.get(player.getUniqueId())) {
            spawnTrailParticle(locationTrail);
        }

        playParticleAndSound(player.getLocation());
        player.teleport(location);
        player.setFallDistance(0);
        recallLocationMap.get(player.getUniqueId()).clear();
        player.heal(calculateBasedOnLevel(BASE_HEAL_AMOUNT, HEAL_AMOUNT_INCREASE_PER_LEVEL, getSkillLevel(player)));
        playParticleAndSound(location);
    }

    private void playParticleAndSound(Location location) {
        location = location.clone().add(0, 1, 0);
        // Create purple dust options (RGB: 0.5, 0, 1 for purple, size: 1.0)
        Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(128, 0, 255), 1.0f);

        // Spawn purple particles around the player
        location.getWorld().spawnParticle(Particle.DUST, location, 50, 0.5, 1.0, 0.5, 0.1, dustOptions);
        dc.getSoundManager().playSound(location, CustomSound.SKILL_RECALL_TELEPORT);
    }

    private void spawnTrailParticle(Location location) {
        location = location.clone().add(0, 1, 0);
        Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(128, 0, 255), 1.0f);

        // Spawn purple particles around the player
        location.getWorld().spawnParticle(Particle.DUST, location, 50, 0, 0, 0, 0.1, dustOptions);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.recall.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.recall.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.recall.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.recall.cooldown_reduction_per_level");
            LOCATION_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.recall.location_duration");
            BASE_HEAL_AMOUNT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.recall.base_heal_amount");
            HEAL_AMOUNT_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.recall.heal_amount_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
