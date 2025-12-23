package com.bindothorpe.champions.domain.sound;

import org.bukkit.Sound;

import java.util.List;

public enum CustomSound {
    SKILL_COOLDOWN_END(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f),
    SKILL_RANGER_SONAR_ARROW_SCAN(Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 0.3f),
    SKILL_RANGER_SONAR_ARROW_DETECT(Sound.BLOCK_NOTE_BLOCK_BELL, 1.2f, 1.0f),
    SKILL_RANGER_SONAR_ARROW_BOUNCE(List.of(Sound.ENTITY_ARROW_SHOOT, Sound.BLOCK_NOTE_BLOCK_HAT), List.of(0.6f, 1.0f), List.of(1.6f, 2.0f)),
    SKILL_ICE_PRISON_ORB_AMBIENT(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f),
    SKILL_ICE_PRISON_ACTIVATE(Sound.BLOCK_GLASS_BREAK, 0.1f, 1.0f),
    SKILL_EXPLOSION_BOMB_ORB_AMBIENT(Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 2.0f),
    SKILL_EXPLOSION_BOMB_ORB_STICK(List.of(Sound.BLOCK_GRAVEL_PLACE, Sound.BLOCK_STONE_BREAK), List.of(1.0f, 1.0f), List.of(0.5f, 1.0f)),
    SKILL_EXPLOSION_BOMB_EXPLODE(Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 2.0f),
    SKILL_LEAP(List.of(Sound.BLOCK_STONE_STEP, Sound.ENTITY_BAT_TAKEOFF), List.of(1.0f, 2.0f), List.of(1.0f, 1.2f)),
    SKILL_MUSIC_CHARGE_MAX(Sound.BLOCK_BELL_USE, 1.0f, 1.0f),
    SKILL_MUSIC_DURATION_MAX(Sound.BLOCK_WOOL_PLACE, 1.0f, 1.0f),
    SKILL_MUSIC_CHARGE(Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f),
    SKILL_MUSIC_CHARGE_START(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f),
    SKILL_MUSIC_CHARGE_END(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f),
    SKILL_WOLFS_POUNCE(Sound.ENTITY_WOLF_AMBIENT,1.0f, 1.2f),
    SKILL_WOLFS_POUNCE_COLLIDE(Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.2f),
    SKILL_TRAMPLE(Sound.BLOCK_ROOTED_DIRT_BREAK, 1.0f, 0.5f),
    SKILL_HEAVY_SWING(Sound.ENTITY_IRON_GOLEM_REPAIR, 2.0f, 1.0f),
    SKILL_BULLS_CHARGE_ACTIVATE(List.of(Sound.ENTITY_ENDERMAN_SCREAM, Sound.BLOCK_STONE_STEP), List.of(1.5f, 49.0f), List.of(0.1f, 1.0f)),
    SKILL_BULLS_CHARGE_HIT(List.of(Sound.ENTITY_ENDERMAN_SCREAM, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR), List.of(1.5f, 1.5f), List.of(0.1f, 0.5f)),
    SKILL_FLESH_HOOK_THROW(Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.8f),
    SKILL_FLESH_HOOK_AMBIENT(Sound.ITEM_FLINTANDSTEEL_USE, 1.4f, 0.8f),
    SKILL_INFERNO_FLAME_SPAWN(Sound.ENTITY_GHAST_SHOOT, 0.1f, 1.0f),
    SKILL_BLIZZARD_SNOWBALL_SPAWN(Sound.BLOCK_SNOW_STEP, 0.1f, 0.5f),
    SKILL_IMMOLATE_AMBIENT(Sound.BLOCK_FIRE_EXTINGUISH, 0.2f, 1.0f),
    SKILL_VOID_AMBIENT(Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.1f, 0.2f),
    SKILL_VOID_HURT(Sound.ENTITY_ENDER_DRAGON_HURT, 2.0f, 1.0f),
    SKILL_STATIC_LAZER_SHOOT(Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 1.0f, 1.8f),
    SKILL_STATIC_LAZER_HIT(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1.0f, 1.8f),
    SKILL_ROPED_ARROW_PRIME(Sound.ENTITY_ENDER_DRAGON_GROWL, 2.5f, 2.0f),
    SKILL_RECALL_TELEPORT(List.of(Sound.ENTITY_PLAYER_TELEPORT, Sound.BLOCK_STONE_STEP), List.of(1.0f, 1.0f), List.of(0.5f, 0.8f)),
    SKILL_SMOKE_BOMB_ACTIVATE(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 2.0f, 0.8f),
    SKILL_BLINK(Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f),
    SKILL_FLASH(List.of(Sound.ENTITY_WITHER_SHOOT, Sound.ENTITY_SILVERFISH_DEATH), List.of(0.4f, 1.2f), List.of(1.0f, 1.6f)),
    SKILL_STAMPEDE_CHARGE(Sound.ENTITY_ZOMBIE_AMBIENT, 2.0f, 1.2f),
    SKILL_STAMPEDE_HIT(Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 0.4f),
    SKILL_LOTUS_TRAP_READY(Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f),
    SKILL_LOTUS_TRAP_HISS(Sound.BLOCK_SPORE_BLOSSOM_STEP, 1.0f, 1.0f),
    SKILL_LOTUS_TRAP_EXPLODE(List.of(Sound.ENTITY_GENERIC_EXPLODE, Sound.BLOCK_SPORE_BLOSSOM_PLACE), List.of(0.3f, 1.0f), List.of(2.0f, 1.0f)),
    SKILL_LOTUS_TRAP_TIMEOUT(Sound.BLOCK_SPORE_BLOSSOM_BREAK, 1.0f, 0.8f),
    SKILL_PHASE_WARP_DASH(Sound.ITEM_TRIDENT_RIPTIDE_1, 0.6f, 1.5f),
    SKILL_PHASE_WARP_WARP(Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.8f),


    CHARGE_SKILL_CHARGE(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f),

    GUI_CLICK(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.6f),
    GUI_CLICK_SKILL_LEVEL_UP(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f),
    GUI_CLICK_SKILL_LEVEL_DOWN(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.6f),
    GUI_CLICK_ERROR(Sound.ENTITY_ITEM_BREAK, 1.0f, 0.6f),
    GUI_CLICK_SUCCESS(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f),

    GAME_START(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f),
    COUNTDOWN(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.6f),
    COUNTDOWN_END(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);


    private final List<Sound> sounds;
    private final List<Float> volumes;
    private final List<Float> pitches;
    private final List<Double> delays;

    CustomSound(Sound sound, float volume, float pitch) {
        this(List.of(sound), List.of(volume), List.of(pitch));
    }
    CustomSound(List<Sound> sounds, List<Float> volumes, List<Float> pitches) {
        this(sounds, volumes, pitches, null);
    }

    CustomSound(List<Sound> sounds, List<Float> volumes, List<Float> pitches, List<Double> delays) {
        if (sounds.size() != volumes.size() || sounds.size() != pitches.size()) {
            throw new IllegalArgumentException("Sounds, volumes, and pitches must be the same size");
        }

        if (delays != null && sounds.size() != delays.size()) {
            throw new IllegalArgumentException("Sounds and delays must be the same size");
        }
        this.sounds = sounds;
        this.volumes = volumes;
        this.pitches = pitches;
        this.delays = delays;
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public List<Float> getVolumes() {
        return volumes;
    }

    public List<Float> getPitches() {
        return pitches;
    }

    public List<Double> getDelays() {
        return delays;
    }
}
