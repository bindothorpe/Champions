package com.bindothorpe.champions.domain.player;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.SkillId;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {

    private final UUID uuid;
    private Map<ClassType, Set<String>> buildIds;
    private Map<UUID, PlayerEffect> effects;
    private String selectedBuildId;

    public PlayerData(UUID uuid, Map<ClassType, Set<String>> buildIds) {
        this.uuid = uuid;
        // In the future this data would come from a MySQL database
        // For now we just pass an empty Map
        if(buildIds == null) {
            buildIds = new HashMap<>();
            for(ClassType type : ClassType.values()) {
                buildIds.put(type, new HashSet<>());
            }
        }
        this.buildIds = buildIds;
        this.effects = new HashMap<>();
    }


    public String getSelectedBuildId() {
        return selectedBuildId;
    }

    public void setSelectedBuildId(String buildId) {
        if(buildId == null) {
            selectedBuildId = null;
            return;
        }

        for(Set<String> builds : buildIds.values()) {
            if(builds.contains(buildId)) {
                selectedBuildId = buildId;
                return;
            }
        }

    }

    public Map<ClassType, Set<String>> getBuildIds() {
        return buildIds;
    }

    public boolean addBuildId(ClassType classType, String buildId) {
        if(buildIds.get(classType).size() == getMaxBuilds())
            return false;

        buildIds.get(classType).add(buildId);
        return true;
    }

    public boolean removeBuildId(String buildId) {
        for(Map.Entry<ClassType, Set<String>> entry: buildIds.entrySet()) {
            if(entry.getValue().contains(buildId)) {
                buildIds.get(entry.getKey()).remove(buildId);
                return true;
            }
        }

        return false;
    }

    public int getBuildCountByClassType(ClassType classType) {
        return buildIds.get(classType).size();
    }

    public int getMaxBuilds() {
        return 7;
    }

    public Map<UUID, PlayerEffect> getEffects() {
        return effects;
    }

    public Map<UUID, PlayerEffect> getEffectsByType(PlayerEffectType type) {
        Map<UUID, PlayerEffect> effectsByType = new HashMap<>();
        for(PlayerEffect effect : effects.values()) {
            if(effect.getType() == type)
                effectsByType.put(effect.getId(), effect);
        }
        return effectsByType;
    }

    public UUID addEffect(PlayerEffect effect) {

        UUID existingEffect = getEffectFromSource(effect.getSource());

        if(existingEffect != null && effect.isMultiply() != effects.get(existingEffect).isMultiply()){
            effects.remove(existingEffect);
        }

        effects.put(effect.getId(), effect);
        updatePlayerEffects(effect.getType());
        return effect.getId();
    }

    public PlayerEffect removeEffect(UUID id) {

        PlayerEffect effect = effects.remove(id);
        if(effect != null) {
            updatePlayerEffects(effect.getType());
        }

        return effect;
    }

    private UUID getEffectFromSource(SkillId source) {
        for(PlayerEffect effect : effects.values()) {
            if(effect.getSource() == source)
                return effect.getId();
        }
        return null;
    }

    private void updatePlayerEffects(PlayerEffectType effectType) {
        List<PlayerEffect> effectsOfType = new ArrayList<>();
        for(PlayerEffect effect : effects.values()) {
            if(effect.getType() == effectType)
                effectsOfType.add(effect);
        }

        effectsOfType = effectsOfType.stream().sorted(PlayerEffect::compareTo).collect(Collectors.toList());
        effectsOfType.forEach(e -> System.out.println("Effect mult: " + e.isMultiply()));
    }
}
