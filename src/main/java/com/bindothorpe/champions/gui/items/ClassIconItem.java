package com.bindothorpe.champions.gui.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClassIconItem extends GuiItem {

    private DomainController dc;
    private UUID uuid;
    private ClassType classType;

    public ClassIconItem(UUID uuid, ClassType classType, DomainController dc) {
        super(new ItemStack(Material.LEATHER_HELMET));
        this.classType = classType;
        this.dc = dc;
        initialize();
        setAction(this::handleClick);
    }

    private void initialize() {
        System.out.println("Initializing class icon item");
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        item.setType(getClassMaterial());
        meta.displayName(getClassName());


        int buildCount = dc.getBuildCountFromPlayer(uuid, classType);


        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));

        lore.add(Component.text("Left click").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                .append(Component.text(" to create a new build.").color(NamedTextColor.YELLOW)));

        lore.add(Component.text(" "));

        lore.add(Component.text("Builds ").color(NamedTextColor.YELLOW)
                .append(Component.text(buildCount).color(NamedTextColor.GREEN))
                .append(Component.text("/3").color(NamedTextColor.YELLOW)));

        meta.lore(lore);

        item.setItemMeta(meta);
        setItem(item);
    }

    private Component getClassName() {
        switch (classType) {
            case BRUTE:
                return Component.text("Brute").decorate(TextDecoration.BOLD).color(NamedTextColor.GRAY);
            case RANGER:
                return Component.text("Ranger").decorate(TextDecoration.BOLD).color(NamedTextColor.GRAY);
            case KNIGHT:
                return Component.text("Knight").decorate(TextDecoration.BOLD).color(NamedTextColor.GRAY);
            case MAGE:
                return Component.text("Mage").decorate(TextDecoration.BOLD).color(NamedTextColor.GRAY);
            default:
                return Component.text("Assassin").decorate(TextDecoration.BOLD).color(NamedTextColor.GRAY);
        }
    }

    private Material getClassMaterial() {
        switch (classType) {
            case BRUTE:
                return Material.DIAMOND_HELMET;
            case RANGER:
                return Material.CHAINMAIL_HELMET;
            case KNIGHT:
                return Material.IRON_HELMET;
            case MAGE:
                return Material.GOLDEN_HELMET;
            default:
                return Material.LEATHER_HELMET;
        }
    }

    private void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClick().equals(ClickType.LEFT)) {
            event.getWhoClicked().sendMessage(Component.text("You left clicked"));
        } else if (event.getClick().equals(ClickType.RIGHT)) {


            String buildId = dc.createEmptyBuild(classType);
            LivingEntity entity = event.getWhoClicked();

            if(dc.getBuildCountFromPlayer(entity.getUniqueId(), classType) == 3) {
                entity.sendMessage(Component.text("You already have 3 builds for this class.").color(NamedTextColor.RED));
                return;
            }

            dc.addBuildIdToPlayer(entity.getUniqueId(), classType, buildId);
            System.out.println(dc.getBuildCountFromPlayer(entity.getUniqueId(), classType));
            dc.openBuildsOverviewGui(entity.getUniqueId());
        }
    }
}
