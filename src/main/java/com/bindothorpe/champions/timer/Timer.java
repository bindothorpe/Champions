package com.bindothorpe.champions.timer;

import com.bindothorpe.champions.ChampionsPlugin;

/**
 * A timer utility class that schedules a delayed task execution in the Bukkit scheduler.
 * This timer runs a specified callback after a configured wait time and provides methods
 * to start, stop, and query the remaining time.
 *
 * @author bindothorpe
 * @version 1.0
 */
public class Timer {

    private final ChampionsPlugin plugin;
    private final double waitTime;
    private final Runnable onTimeout;

    private float startTimeInMillis;
    private int taskId;
    private boolean isRunning = false;

    /**
     * Constructs a new Timer instance.
     *
     * @param plugin the ChampionsPlugin instance used to access the Bukkit scheduler
     * @param waitTime the duration to wait before executing the timeout callback, in seconds
     * @param onTimeout the callback to execute when the timer completes
     */
    public Timer(ChampionsPlugin plugin, double waitTime, Runnable onTimeout) {
        this.plugin = plugin;
        this.waitTime = waitTime;
        this.onTimeout = onTimeout;
    }

    /**
     * Starts the timer. Schedules a delayed task that will execute the timeout callback
     * after the configured wait time. The timer must not already be running.
     * <p>
     * The delay is calculated as {@code waitTime * 20} ticks, where 20 ticks equals 1 second
     * in Minecraft's game loop.
     */
    public void start() {
        startTimeInMillis = System.currentTimeMillis();
        taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                plugin,
                () -> {
                    onTimeout.run();
                    isRunning = false;
                },
                (long) (waitTime * 20L)
        );
        isRunning = true;
    }

    /**
     * Stops the timer if it is currently running. Cancels the scheduled task and resets
     * the timer state. If the timer is not running, this method has no effect.
     */
    public void stop() {
        if(!isRunning) return;
        startTimeInMillis = -1L;
        plugin.getServer().getScheduler().cancelTask(taskId);
        taskId = -1;
        isRunning = false;
    }

    /**
     * Gets the remaining time until the timer completes.
     *
     * @return the time remaining in milliseconds, or -1 if the timer is not currently running
     */
    public double getTimeLeft() {
        if(!isRunning) return -1;

        return startTimeInMillis + (waitTime * 1000L) - System.currentTimeMillis();
    }
}