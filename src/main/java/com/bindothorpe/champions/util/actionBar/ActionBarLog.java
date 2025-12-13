package com.bindothorpe.champions.util.actionBar;

import net.kyori.adventure.text.Component;

public record ActionBarLog(long timestamp, ActionBarPriority priority, Component message) {

}
