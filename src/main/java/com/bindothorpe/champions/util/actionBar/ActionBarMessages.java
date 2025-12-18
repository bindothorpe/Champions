package com.bindothorpe.champions.util.actionBar;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ActionBarMessages {

    private final static long ACTION_BAR_DURATION = 50;

    private final Set<ActionBarLog> actionBarLogs = new HashSet<>();


    public void addActionBarLog(ActionBarLog actionBarLog) {
        actionBarLogs.add(actionBarLog);
        removeOldActionBarLogs();
    }

    public ActionBarLog getCurrentActionBarLog() {
        removeOldActionBarLogs();
        return actionBarLogs.stream()
                .filter(log -> System.currentTimeMillis() - log.timestamp() <= ACTION_BAR_DURATION)
                .min(Comparator.comparingInt(log -> -log.priority().ordinal()))
                .orElse(null);
    }

    private void removeOldActionBarLogs() {
        actionBarLogs.removeIf((log) -> System.currentTimeMillis() - log.timestamp() > ActionBarMessages.ACTION_BAR_DURATION);
    }
}
