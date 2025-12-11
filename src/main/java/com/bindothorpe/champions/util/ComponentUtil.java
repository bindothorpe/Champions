package com.bindothorpe.champions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
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

    public static <T extends Number> Component skillValuesBasedOnLevel(T baseValue, T increasePerLevel, int level, int maxLevel, NamedTextColor highlightColor) {
        return skillValuesBasedOnLevel(baseValue, increasePerLevel, level, maxLevel, false, highlightColor);
    }

    public static <T extends Number> Component skillValuesBasedOnLevel(T baseValue, T increasePerLevel, int level, int maxLevel, boolean isPercentile, NamedTextColor highlightColor) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < maxLevel; i++) {
            if (i > 0) {
                builder.append(Component.text(" / ").color(NamedTextColor.GRAY));
            }
            if(baseValue instanceof Long || baseValue instanceof Integer) {
                builder.append(Component.text(String.format("%d%s",calculateBasedOnLevel(baseValue, increasePerLevel, i + 1), isPercentile ? "%" : "")).color(level == i + 1 ? highlightColor : NamedTextColor.GRAY));
            } else {
                builder.append(Component.text(String.format("%.1f%s", calculateBasedOnLevel(baseValue, increasePerLevel, i + 1), isPercentile ? "%" : "")).color(level == i + 1 ? highlightColor : NamedTextColor.GRAY));
            }

        }
        return builder.build();
    }

    private static <T extends Number> T calculateBasedOnLevel(T baseValue, T increasePerLevel, int level) {
        if (baseValue instanceof Integer) {
            return (T) Integer.valueOf(baseValue.intValue() + increasePerLevel.intValue() * (level - 1));
        } else if (baseValue instanceof Double) {
            return (T) Double.valueOf(baseValue.doubleValue() + increasePerLevel.doubleValue() * (level - 1));
        } else if (baseValue instanceof Float) {
            return (T) Float.valueOf(baseValue.floatValue() + increasePerLevel.floatValue() * (level - 1));
        } else if (baseValue instanceof Long) {
            return (T) Long.valueOf(baseValue.longValue() + increasePerLevel.longValue() * (level - 1));
        }
        throw new UnsupportedOperationException("Unsupported number type: " + baseValue.getClass());
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
        return Component.text("Passive: ").color(NamedTextColor.WHITE);
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

    public static Component skillCharge(String skillName, boolean showPercentage, int charge, int maxCharge, int width, String symbol) {
        Component component = Component.text(skillName).decorate(TextDecoration.BOLD).color(NamedTextColor.WHITE)
                .append(Component.text(" ").decoration(TextDecoration.BOLD, false)
                        .append(chargeBar(charge, maxCharge, width, symbol)));

        if(showPercentage) {

            double percentage = Math.min((double) charge / (double) maxCharge, 1);
            component = component.append(Component.text(" ").decoration(TextDecoration.BOLD, false)
                    .append(Component.text(String.format("%d%%", (int) (percentage * 100))).color(NamedTextColor.WHITE)));
        }

        return component;
    }

    public static Component skillCharge(String skillName, boolean showPercentage, int charge, int maxCharge) {
        return skillCharge(skillName, showPercentage, charge, maxCharge, 40, "|");
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

    /**
     * Wraps a Component into multiple lines while preserving formatting
     * @param component The component to wrap
     * @param maxLength Maximum characters per line (excluding color codes)
     * @return List of Components, one per line
     */
    public static List<Component> wrapComponentWithFormatting(Component component, int maxLength) {
        List<Component> lines = new ArrayList<>();
        List<StyledSegment> segments = flattenComponent(component);

        TextComponent.Builder currentLine = Component.text();
        int currentLength = 0;

        for (StyledSegment segment : segments) {
            String[] words = segment.text.split(" ", -1);

            for (int i = 0; i < words.length; i++) {
                String word = words[i];

                // Add space before word if not at start of line
                String toAdd = (currentLength > 0 && i > 0) ? " " + word : word;
                int addLength = toAdd.length();

                if (currentLength + addLength > maxLength && currentLength > 0) {
                    // Line is full, save it and start new line
                    lines.add(currentLine.build());
                    currentLine = Component.text();
                    currentLength = 0;
                    toAdd = word; // Don't add leading space on new line
                    addLength = word.length();
                }

                // Add the word with its style
                currentLine.append(Component.text(toAdd, segment.style));
                currentLength += addLength;
            }
        }

        // Add the last line if it has content
        Component lastLine = currentLine.build();
        if (!lastLine.children().isEmpty() || (lastLine instanceof TextComponent && !((TextComponent) lastLine).content().isEmpty())) {
            lines.add(lastLine);
        }

        return lines;
    }

    private static List<StyledSegment> flattenComponent(Component component) {
        List<StyledSegment> segments = new ArrayList<>();
        flattenRecursive(component, Style.empty(), segments);
        return segments;
    }

    private static void flattenRecursive(Component component, Style inheritedStyle, List<StyledSegment> segments) {
        Style currentStyle = inheritedStyle.merge(component.style());

        if (component instanceof TextComponent) {
            String content = ((TextComponent) component).content();
            if (!content.isEmpty()) {
                segments.add(new StyledSegment(content, currentStyle));
            }
        }

        for (Component child : component.children()) {
            flattenRecursive(child, currentStyle, segments);
        }
    }

    private static class StyledSegment {
        final String text;
        final Style style;

        public StyledSegment(String text, Style style) {
            this.text = text;
            this.style = style;
        }
    }

}
