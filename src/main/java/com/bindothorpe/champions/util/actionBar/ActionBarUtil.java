package com.bindothorpe.champions.util.actionBar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ActionBarUtil {

    private final static Map<UUID, ActionBarMessages> actionBarMessages = new HashMap<>();

    private ActionBarUtil() {

    }

    public static void sendMessage(@NotNull Player player, Component message, ActionBarPriority priority) {
        UUID uuid = player.getUniqueId();
        actionBarMessages.computeIfAbsent(uuid, k -> new ActionBarMessages());

        actionBarMessages.get(uuid).addActionBarLog(new ActionBarLog(System.currentTimeMillis(), priority, message));

        Component messageToSend = actionBarMessages.get(uuid).getCurrentActionBarLog().message();
        player.sendActionBar(messageToSend);
    }



}
