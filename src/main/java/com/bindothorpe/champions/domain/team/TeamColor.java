package com.bindothorpe.champions.domain.team;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;

public enum TeamColor {
    BLUE(NamedTextColor.BLUE, Color.BLUE),
    RED(NamedTextColor.RED, Color.RED);

    private final NamedTextColor textColor;
    private final Color color;

    TeamColor(NamedTextColor textColor, Color color) {
        this.color = color;
        this.textColor = textColor;
    }

    public NamedTextColor getTextColor() {
        return textColor;
    }

    public Color getColor() {
        return color;
    }
}
