package com.bindothorpe.champions.gui.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClassIconItem extends GuiItem {

    private final DomainController dc;
    private final UUID uuid;
    private final ClassType classType;
    private final boolean showBuildCount;
    private final boolean showLeftclickAction;
    private final boolean showRightclickAction;
    private final boolean showDescription;

    public ClassIconItem(UUID uuid, ClassType classType, DomainController dc, boolean showBuildCount, boolean showLeftclickAction, boolean showRightclickAction, boolean showDescription) {
        super(new ItemStack(Material.LEATHER_HELMET));
        this.dc = dc;
        this.uuid = uuid;
        this.classType = classType;
        this.showBuildCount = showBuildCount;
        this.showLeftclickAction = showLeftclickAction;
        this.showRightclickAction = showRightclickAction;
        this.showDescription = showDescription;

        initialize();
        setAction(this::handleClick);
    }

    private void initialize() {
        ItemStack item = getItem();
        item.setType(getClassMaterial(classType));

        item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(TextUtil.camelCasing(classType.toString())));
        meta.lore(new ArrayList<>());

        if (showDescription) {
            //TODO: Add description to classType
//            meta.lore(TextUtil.wrapText(classType.getDescription(), 30));
        }

        if (showBuildCount) {
            List<Component> lore = meta.lore();
            lore.add(Component.text(" "));
            lore.add(Component.text("Builds: ").color(NamedTextColor.GRAY)
                    .append(Component.text(dc.getBuildCountByClassTypeForPlayer(classType, uuid)).color(NamedTextColor.YELLOW))
                    .append(Component.text("/"))
                    .append(Component.text(dc.getMaxBuildsForPlayer(uuid))));
            meta.lore(lore);
        }

        if (showLeftclickAction || showRightclickAction) {
            List<Component> lore = meta.lore();
            lore.add(Component.text(" "));
            if (showLeftclickAction) {
                lore.add(Component.text("Left-click").color(NamedTextColor.YELLOW)
                        .append(Component.text(" to view builds").color(NamedTextColor.GRAY)));
            }

            if (showRightclickAction) {
                lore.add(Component.text("Right-click").color(NamedTextColor.YELLOW)
                        .append(Component.text(" to create a new build").color(NamedTextColor.GRAY)));
            }

            meta.lore(lore);
        }

        item.setItemMeta(meta);
    }


    private void handleClick(InventoryClickEvent event) {

        if (event.getClick().equals(ClickType.LEFT) && showLeftclickAction) {
            dc.openBuildsOverviewGui(uuid, classType);
        }

        int buildCount = dc.getBuildCountByClassTypeForPlayer(classType, uuid);
        int maxBuildCount = dc.getMaxBuildsForPlayer(uuid);

        if (event.getClick().equals(ClickType.RIGHT)
                && showRightclickAction
                && buildCount < maxBuildCount) {


            String buildId = dc.createEmptyBuild(classType);
            dc.addBuildIdToPlayer(uuid, classType, buildId);
            dc.openBuildsOverviewGui(uuid, classType);
        }


    }

    private Material getClassMaterial(ClassType classType) {
        return switch (classType) {
            case BRUTE -> Material.DIAMOND_HELMET;
            case RANGER -> Material.CHAINMAIL_HELMET;
            case KNIGHT -> Material.IRON_HELMET;
            case MAGE -> Material.GOLDEN_HELMET;
            default -> Material.LEATHER_HELMET;
        };
    }
}
