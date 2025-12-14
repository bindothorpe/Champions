package com.bindothorpe.champions.domain.sound;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SoundManager {

    private static SoundManager instance;
    private final DomainController dc;

    private SoundManager(DomainController dc) {
        this.dc = dc;
    }

    public static SoundManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new SoundManager(dc);
        }
        return instance;
    }

    /**
     * Plays a sound for everyone on the given location
     * @param location The location where it should be played
     * @param sound The sound that should be played
     */
    public void playSound(Location location, CustomSound sound) {
        List<Sound> sounds = sound.getSounds();
        List<Float> volumes = sound.getVolumes();
        List<Float> pitches = sound.getPitches();
        List<Double> delays = sound.getDelays();

        for (int i = 0; i < sounds.size(); i++) {
            if (delays == null || delays.isEmpty()) {
                location.getWorld().playSound(location, sounds.get(i), volumes.get(i), pitches.get(i));
            } else {
                int finalI = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        location.getWorld().playSound(location, sounds.get(finalI), volumes.get(finalI), pitches.get(finalI));
                    }
                }.runTaskLater(dc.getPlugin(), (long) (delays.get(i) * 20L));
            }
        }
    }

    public void playSound(Player player, Location location, CustomSound sound, double pitchMultiplier) {
        if (player == null) {
            return;
        }

        List<Sound> sounds = sound.getSounds();
        List<Float> volumes = sound.getVolumes();
        List<Float> pitches = sound.getPitches();
        List<Double> delays = sound.getDelays();

        for (int i = 0; i < sounds.size(); i++) {
            if (delays == null || delays.isEmpty()) {
                float pitch = pitches.get(i);
                player.playSound(location, sounds.get(i), volumes.get(i), (float) (pitch + (pitch * pitchMultiplier)));
            } else {
                int finalI = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.playSound(location, sounds.get(finalI), volumes.get(finalI), pitches.get(finalI));
                    }
                }.runTaskLater(dc.getPlugin(), (long) (delays.get(i) * 20L));
            }
        }
    }

    public void playSound(Player player, Location location, CustomSound sound, float pitchOverride) {
        if (player == null) {
            return;
        }

        List<Sound> sounds = sound.getSounds();
        List<Float> volumes = sound.getVolumes();
        List<Float> pitches = sound.getPitches();
        List<Double> delays = sound.getDelays();

        for (int i = 0; i < sounds.size(); i++) {
            if (delays == null || delays.isEmpty()) {
                float pitch = pitches.get(i);
                player.playSound(location, sounds.get(i), volumes.get(i), pitchOverride);
            } else {
                int finalI = i;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.playSound(location, sounds.get(finalI), volumes.get(finalI), pitchOverride);
                    }
                }.runTaskLater(dc.getPlugin(), (long) (delays.get(i) * 20L));
            }
        }
    }

    /**
     * Plays a sound for the player
     * @param player The player that should hear the sound
     * @param sound The sound that should be played
     */
    public void playSound(Player player, CustomSound sound) {
        playSound(player, player.getLocation(), sound, 0);
    }

    public void playSound(Player player, CustomSound sound, double pitchMultiplier) {
        playSound(player, player.getLocation(), sound, pitchMultiplier);
    }

    public void playSound(Player player, CustomSound sound, float pitchOverride) {
        playSound(player, player.getLocation(), sound, pitchOverride);
    }

}
