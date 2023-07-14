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
