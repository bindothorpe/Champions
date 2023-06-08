package com.bindothorpe.champions.domain.effect;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerEffectManager {

    private static PlayerEffectManager instance;

    private final DomainController dc;

    private final Map<UUID, Map<UUID, PlayerEffect>> playerEffectMap;
    private final Map<UUID, BukkitTask> taskMap;

    private PlayerEffectManager(DomainController dc) {
        this.dc = dc;
        this.playerEffectMap = new HashMap<>();
        this.taskMap = new HashMap<>();
    }

    public static PlayerEffectManager getInstance(DomainController dc) {
        if (instance == null)
            instance = new PlayerEffectManager(dc);
        return instance;
    }

    public Map<UUID, PlayerEffect> getEffectsFromPlayer(UUID uuid) {
        if (!playerEffectMap.containsKey(uuid))
            playerEffectMap.put(uuid, new HashMap<>());
        return playerEffectMap.get(uuid);
    }

    public Map<UUID, PlayerEffect> getEffectsFromPlayerByType(UUID uuid, PlayerEffectType playerEffectType) {
        // Check if the player already has an effect map
        if (!playerEffectMap.containsKey(uuid))
            playerEffectMap.put(uuid, new HashMap<>());

        return playerEffectMap.get(uuid).values().stream()
                .filter(playerEffect -> playerEffect.getType() == playerEffectType)
                .collect(Collectors.toMap(PlayerEffect::getId, playerEffect -> playerEffect));
    }

    public void addEffectToPlayer(UUID uuid, PlayerEffect playerEffect) {
        // Check if the player already has an effect map
        if (!playerEffectMap.containsKey(uuid)) {

            // Add player to the map
            playerEffectMap.put(uuid, new HashMap<>());
        }

        // Get the existing effect with the same source
        PlayerEffect existingEffect = getEffectFromPlayerBySource(uuid, playerEffect.getSource());

        // Check if the existing effect is null, check if the existing effect is mult/sum equal to the new effect
        if (existingEffect != null && playerEffect.isMultiply() == existingEffect.isMultiply()) {

            // Remove the existing effect
            playerEffectMap.get(uuid).remove(existingEffect.getId());
            taskMap.remove(existingEffect.getId());
        }

        // Add the new effect
        playerEffectMap.get(uuid).put(playerEffect.getId(), playerEffect);

        if (playerEffect.getDuration() != -1) {

            // Remove the effect after the duration
            taskMap.put(playerEffect.getId(), new BukkitRunnable() {
                @Override
                public void run() {
                    removeEffectFromPlayer(uuid, playerEffect.getId());
                }
            }.runTaskLater(dc.getPlugin(), (long) (playerEffect.getDuration() * 20)));
        }

        // Update the player effects for the player
        updatePlayerEffectsForPlayer(uuid, playerEffect);
    }

    public void removeEffectFromPlayer(UUID uuid, UUID id) {
        // Check if the player already has an effect map
        if (!playerEffectMap.containsKey(uuid))
            playerEffectMap.put(uuid, new HashMap<>());

        // Remove the effect from the map
        PlayerEffect playerEffect = playerEffectMap.get(uuid).remove(id);

        // Check if the effect was removed
        if (playerEffect != null) {
            // Update the player effects for the player
            updatePlayerEffectsForPlayer(uuid, playerEffect);
        }

    }


    private void updatePlayerEffectsForPlayer(UUID uuid, PlayerEffect playerEffect) {
        // Check if the player already has an effect map
        if (!playerEffectMap.containsKey(uuid))
            playerEffectMap.put(uuid, new HashMap<>());

        // Create a list to store the effects of the type
        List<PlayerEffect> effectsOfType = new ArrayList<>();

        PlayerEffectType type = playerEffect.getType();

        // Add all the effects of the type to the list
        for (PlayerEffect effect : playerEffectMap.get(uuid).values()) {
            if (effect.getType() == type) {
                effectsOfType.add(effect);
            }
        }

        // Sort the effects by modification / multiplication
        effectsOfType = effectsOfType.stream().sorted(PlayerEffect::compareTo).collect(Collectors.toList());


        // Apply the effect
        playerEffect.applyEffect(uuid, effectsOfType);

    }


    private PlayerEffect getEffectFromPlayerBySource(UUID uuid, SkillId source) {
        if (!playerEffectMap.containsKey(uuid))
            playerEffectMap.put(uuid, new HashMap<>());

        for (PlayerEffect effect : playerEffectMap.get(uuid).values()) {
            if (effect.getSource() == source)
                return effect;
        }
        return null;
    }
}
