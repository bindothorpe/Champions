package com.bindothorpe.champions.gui.items.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.build.UpdateBuildEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        if(dc.getBuildManager().getSkillFromBuild(buildId, dc.getSkillManager().getSkillType(skillId)) == skillId) {
            this.skillLevel = dc.getBuildManager().getSkillLevelFromBuild(buildId, dc.getSkillManager().getSkillType(skillId));
        } else {
            this.skillLevel = 0;
        }

        initialize();
        setAction(this::handleClick);

    }

    private void handleClick(InventoryClickEvent event) {
        boolean success = false;
        boolean levelUpFlag = false;
        if(event.isLeftClick()) {
            success = dc.getBuildManager().levelUpSkillForBuild(buildId, skillId);
            levelUpFlag = true;
        } else if (event.isRightClick()) {
            success = dc.getBuildManager().levelDownSkillForBuild(buildId, skillId);

            Build build = dc.getBuildManager().getBuild(buildId);
            int level = build.getSkillLevel(skillId.getSkillType());

            if(success && level == 0) {
                dc.getSkillManager().unequipSkillForPlayer(event.getWhoClicked().getUniqueId(), skillId);
            }


        }

        Player player = (Player) event.getWhoClicked();

        if(success) {

            if(levelUpFlag) {
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_SKILL_LEVEL_UP);
            } else {
                dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_SKILL_LEVEL_DOWN);
            }

            dc.getGuiManager().openEditBuildGui(event.getWhoClicked().getUniqueId(), buildId, buildNumber);
            Bukkit.getPluginManager().callEvent(new UpdateBuildEvent(dc.getBuildManager().getBuild(buildId), event.getWhoClicked().getUniqueId()));
        } else {

            dc.getSoundManager().playSound(player, CustomSound.GUI_CLICK_ERROR);
        }
    }

    private void initialize() {
        ItemStack item = getItem();
        ItemMeta meta = item.getItemMeta();

        int levelUpCost = dc.getSkillManager().getSkillLevelUpCost(skillId);

        if(skillLevel > 0) {
            item.setAmount(skillLevel);
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.displayName(Component.text(dc.getSkillManager().getSkillName(skillId)).color(NamedTextColor.GREEN));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));
        lore.add(Component.text("Level-up Cost: ").color(NamedTextColor.GRAY)
                .append(Component.text(levelUpCost).color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(levelUpCost == 1 ? " Skill Point" : " Skill Points").color(NamedTextColor.LIGHT_PURPLE)));
        lore.add(Component.text("Level: ").color(NamedTextColor.GRAY)
                .append(Component.text(skillLevel).color(NamedTextColor.YELLOW))
                .append(Component.text("/").color(NamedTextColor.GRAY))
                .append(Component.text(dc.getSkillManager().getSkillMaxLevel(skillId)).color(NamedTextColor.GRAY)));

        if(dc.getSkillManager().getSkillCooldownDuration(skillId) != null) {
            lore.add(Component.text(" "));

            lore.add(Component.text("Cooldown: ").color(NamedTextColor.GRAY)
                    .append(ComponentUtil.skillLevelValues(skillLevel, dc.getSkillManager().getSkillCooldownDuration(skillId), NamedTextColor.YELLOW)));
        }

        lore.add(Component.text(" "));

        lore.addAll(dc.getSkillManager().getSkillDescription(skillId, skillLevel));

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
