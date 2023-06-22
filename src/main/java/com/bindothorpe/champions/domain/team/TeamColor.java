package com.bindothorpe.champions.domain.team;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum TeamColor {
    BLUE(NamedTextColor.BLUE),
    RED(NamedTextColor.RED);

    private final NamedTextColor color;

    TeamColor(NamedTextColor color) {
        this.color = color;
    }

    public NamedTextColor getColor() {
        return color;
    }
}
