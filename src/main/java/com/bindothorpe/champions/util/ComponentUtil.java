package com.bindothorpe.champions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Collections;
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

    public static Component leftClick(boolean capitalized) {
        return Component.text(capitalized ? "Left-click " : "left-click ").color(NamedTextColor.YELLOW);
    }

    public static Component leftClick() {
        return leftClick(false);
    }


    public static Component rightClick(boolean capitalized) {
        return Component.text(capitalized ? "Right-click " : "right-click ").color(NamedTextColor.YELLOW);
    }
    public static Component rightClick() {
        return rightClick(false);
    }

    public static Component passive() {
        return Component.text("Passive ").color(NamedTextColor.WHITE);
    }

    public static Component active() {
        return Component.text("Active: ").color(NamedTextColor.WHITE);
    }

    public static Component cooldownRemainingBar(String skillName, double cooldownRemainingPercentage, double cooldownRemainingInSeconds) {
        return cooldownRemainingBar(skillName, cooldownRemainingPercentage, cooldownRemainingInSeconds, 40, "|");
    }

    public static Component cooldownRemainingBar(String skillName, double cooldownRemainingPercentage, double cooldownRemainingInSeconds, int width, String symbol) {
        int completedWidth = (int) (cooldownRemainingPercentage * width);
        int remainingWidth = width - completedWidth;

        String cooldownCompleted = String.join("", Collections.nCopies(completedWidth, symbol));
        String cooldownRemaining = String.join("", Collections.nCopies(remainingWidth, symbol));

        return Component.text(skillName).decorate(TextDecoration.BOLD).color(NamedTextColor.WHITE)
                .append(Component.text(" "))
                .append(Component.text(cooldownRemaining).color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false))
                .append(Component.text(cooldownCompleted).color(NamedTextColor.RED).decoration(TextDecoration.BOLD, false))
                .append(Component.text(" ").decoration(TextDecoration.BOLD, false))
                .append(Component.text(String.format("%.1fs", cooldownRemainingInSeconds)).color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));
    }

    public static Component chargeBar(int charge, int maxCharge) {
        return chargeBar(charge, maxCharge, 40, "|");
    }
    public static Component chargeBar(int charge, int maxCharge, int width, String symbol) {
        int adjustedCharge = (int) ((double) charge / maxCharge * width);

        // Ensure that adjustedCharge does not exceed width
        adjustedCharge = Math.min(adjustedCharge, width);

        int remainingCharge = width - adjustedCharge;

        // Ensure that remainingCharge is not less than zero
        remainingCharge = Math.max(remainingCharge, 0);

        // Generate the charged and toCharge strings
        String charged = String.join("", Collections.nCopies(adjustedCharge, symbol));
        String toCharge = String.join("", Collections.nCopies(remainingCharge, symbol));

        // Create the components
        Component chargedComponent = Component.text(charged).color(NamedTextColor.YELLOW);
        Component toChargeComponent = Component.text(toCharge).color(NamedTextColor.GRAY);

        // Return the combined component
        return chargedComponent.append(toChargeComponent);
    }

}
