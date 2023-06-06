package com.bindothorpe.champions.gui.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildItem extends GuiItem {

    private String buildId;
    private int buildNumber;
    private DomainController dc;

    public BuildItem(String buildId, int buildNumber, DomainController dc) {
        super(new ItemStack(Material.ARMOR_STAND));
        this.buildId = buildId;
        this.buildNumber = buildNumber;
        this.dc = dc;
        initialize();
        setAction(this::handleClick);
    }

    private void initialize() {
        // Get the skills for the build
        Map<SkillId, Integer> skills = dc.getBuild(buildId).getSkills();

        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        // Set the display name
        meta.displayName(Component.text("Build ").append(Component.text(buildNumber)));
        List<Component> newLore = new ArrayList<>();
        newLore.add(Component.text(" "));


        for(SkillType skillType : dc.getSkillTypes()) {
            String skillTypeName = TextUtil.enumToCamelCase(skillType.toString());

        }

        // Add the skills to the lore
//        for(Map.Entry<SkillId, Integer> skill : skills.entrySet()) {
//
//            if(skill.getValue() == null) {
//                newLore.add(Component.text(skill.getKey().toString()).color(NamedTextColor.YELLOW)
//                        .append(Component.text(": None")));
//            } else {
//                newLore.add(Component.text(skill.getKey().toString()).color(NamedTextColor.YELLOW)
//                        .append(Component.text(": "))
//                        .append(Component.text(dc.getSkillName(skill.getValue())))
//                        .append(Component.text(" "))
//                        .append(Component.text(skillLevels.get(skill.getValue()))).color(NamedTextColor.GREEN)
//                        .append(Component.text("/"))
//                        .append(Component.text(dc.getSkillMaxLevel(skill.getValue()))));
//            }
//
//
//        }


        newLore.add(Component.text(" "));

        // Add the instructions to the lore
        newLore.add(Component.text("Left click").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                .append(Component.text(" to equip").color(NamedTextColor.YELLOW)));

        newLore.add(Component.text("Right click").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                .append(Component.text(" to edit").color(NamedTextColor.YELLOW)));

        meta.lore(newLore);
        item.setItemMeta(meta);
    }

    private void handleClick(InventoryClickEvent event) {
        if(event.isLeftClick()) {
            dc.equipBuildForPlayer(event.getWhoClicked().getUniqueId(), buildId);
            event.getWhoClicked().getOpenInventory().close();
        } else if (event.isRightClick()) {
            dc.openEditBuildGui(event.getWhoClicked().getUniqueId(), buildId);
        }
    }


}
