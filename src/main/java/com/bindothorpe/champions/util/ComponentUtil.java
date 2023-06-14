package com.bindothorpe.champions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class ComponentUtil {

    public static <T> Component skillLevelValues(int skillLevel, List<T> values, NamedTextColor highlightColor) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(Component.text(" / ").color(NamedTextColor.GRAY));
            }
            builder.append(Component.text(String.valueOf(values.get(i))).color(skillLevel == i + 1 ? highlightColor : NamedTextColor.GRAY));
        }
        return builder.build();
    }

    public static Component leftClick() {
        return Component.text("left-click ").color(NamedTextColor.YELLOW);
    }

    public static Component rightClick() {
        return Component.text("right-click ").color(NamedTextColor.YELLOW);
    }

    public static Component passive() {
        return Component.text("Passive ").color(NamedTextColor.WHITE);
    }

    public static Component active() {
        return Component.text("Active: ").color(NamedTextColor.WHITE);
    }

}
