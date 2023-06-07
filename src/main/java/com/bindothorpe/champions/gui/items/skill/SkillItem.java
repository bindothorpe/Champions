package com.bindothorpe.champions.gui.items.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillItem extends GuiItem {

    private final DomainController dc;
    private final String buildId;
    private final int buildNumber;
    private final SkillId skillId;
    private final int skillLevel;

    public SkillItem(String buildId, int buildNumber, SkillId skillId, DomainController dc) {
        super(new ItemStack(Material.BOOK));
        this.dc = dc;
        this.buildId = buildId;
        this.buildNumber = buildNumber;
        this.skillId = skillId;
        this.skillLevel = dc.getSkillLevelFromBuild(buildId, dc.getSkillType(skillId));

        initialize();
        setAction(this::handleClick);
    }

    private void handleClick(InventoryClickEvent event) {
        boolean success = false;
        if(event.isLeftClick()) {
            success = dc.levelUpSkillForBuild(buildId, skillId);
        } else if (event.isRightClick()) {
            success = dc.levelDownSkillForBuild(buildId, skillId);
        }

        if(success) {
            //TODO: play sound effect
            dc.openEditBuildGui(event.getWhoClicked().getUniqueId(), buildId, buildNumber);
        } else {
            //TODO: play error effect
        }
    }

    private void initialize() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        int levelUpCost = dc.getSkillLevelUpCost(skillId);

        if(skillLevel > 0) {
            item.setAmount(skillLevel);
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.displayName(Component.text(dc.getSkillName(skillId)).color(NamedTextColor.GREEN));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));
        lore.add(Component.text("Level-up Cost: ").color(NamedTextColor.GRAY)
                .append(Component.text(levelUpCost).color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(levelUpCost == 1 ? " Skill Point" : " Skill Points").color(NamedTextColor.LIGHT_PURPLE)));
        lore.add(Component.text("Level: ").color(NamedTextColor.GRAY)
                .append(Component.text(skillLevel).color(NamedTextColor.YELLOW))
                .append(Component.text("/").color(NamedTextColor.GRAY))
                .append(Component.text(dc.getSkillMaxLevel(skillId)).color(NamedTextColor.GRAY)));

        lore.add(Component.text(" "));

        lore.add(Component.text("Cooldown: ").color(NamedTextColor.GRAY)
                .append(getCooldownTextComponent(dc.getSkillCooldownDuration(skillId), skillLevel)));


        lore.add(Component.text(" "));

        lore.addAll(dc.getSkillDescription(skillId, skillLevel));

        lore.add(Component.text(" "));
        lore.add(Component.text("Left-click").color(NamedTextColor.YELLOW)
                .append(Component.text(" to level up").color(NamedTextColor.GRAY)));
        lore.add(Component.text("Right-click").color(NamedTextColor.YELLOW)
                .append(Component.text(" to level down").color(NamedTextColor.GRAY)));


        meta.lore(lore);
        getItem().setItemMeta(meta);
    }

    private Component getCooldownTextComponent(List<Double> cooldowns, int skillLevel) {
        TextComponent.Builder builder = Component.text();
        for(int i = 0; i < cooldowns.size(); i++) {
            if(i > 0) {
                builder.append(Component.text(" / ").color(NamedTextColor.GRAY));
            }
            builder.append(Component.text(cooldowns.get(i)).color(skillLevel == i + 1 ? NamedTextColor.YELLOW : NamedTextColor.GRAY));
        }
        return builder.build();
    }
}
