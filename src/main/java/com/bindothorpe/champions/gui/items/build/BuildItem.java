package com.bindothorpe.champions.gui.items.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuildItem extends GuiItem {

    private UUID uuid;
    private String buildId;
    private ClassType classType;
    private int buildNumber;
    private DomainController dc;

    public BuildItem(UUID uuid, String buildId, int buildNumber, DomainController dc) {
        super(new ItemStack(Material.ARMOR_STAND));
        this.uuid = uuid;
        this.buildId = buildId;
        this.classType = dc.getBuildManager().getClassTypeFromBuild(buildId);
        this.buildNumber = buildNumber;
        this.dc = dc;
        initialize();
        setAction(this::handleClick);
    }

    private void handleClick(InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getClick().isLeftClick()) {
            if(buildId.equals(dc.getPlayerManager().getSelectedBuildIdFromPlayer(uuid))) {
                dc.getBuildManager().unequipBuildForPlayer(uuid);
            } else {
                dc.getBuildManager().equipBuildForPlayer(uuid, buildId);
            }
            dc.getGuiManager().openBuildsOverviewGui(uuid, classType);
        } else if (inventoryClickEvent.getClick().isRightClick()) {
            dc.getGuiManager().openEditBuildGui(uuid, buildId, buildNumber);
        }
    }

    private void initialize() {
        ItemMeta meta = getItem().getItemMeta();

        boolean isSelected = buildId.equals(dc.getPlayerManager().getSelectedBuildIdFromPlayer(uuid));

        meta.displayName(Component.text("Build ").color(NamedTextColor.WHITE)
                .append(Component.text(buildNumber))
                .append(Component.text(isSelected ? " (Selected)" : "").color(NamedTextColor.GREEN)));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));

        for (SkillType skillType : SkillType.values()) {
            if (skillType.equals(SkillType.CLASS_PASSIVE)) {
                continue;
            }

            SkillId skillId = dc.getBuildManager().getSkillFromBuild(buildId, skillType);
            String skillName = skillId == null ? "None" : dc.getSkillManager().getSkillName(skillId);
            String skillLevelString = skillId == null ? "" : String.valueOf(dc.getBuildManager().getSkillLevelFromBuild(buildId, skillType));

            lore.add(Component.text(TextUtil.camelCasing(skillType.toString())).color(NamedTextColor.GRAY)
                    .append(Component.text(": "))
                    .append(Component.text(skillName).color(NamedTextColor.YELLOW))
                    .append(Component.text(" "))
                    .append(Component.text(skillLevelString).color(NamedTextColor.YELLOW)));
        }

        lore.add(Component.text(" "));

        lore.add(Component.text("Left-click").color(NamedTextColor.YELLOW)
                .append(Component.text(" to select build").color(NamedTextColor.GRAY)));
        lore.add(Component.text("Right-click").color(NamedTextColor.YELLOW)
                .append(Component.text(" to edit build").color(NamedTextColor.GRAY)));

        meta.lore(lore);
        getItem().setItemMeta(meta);
    }
}
