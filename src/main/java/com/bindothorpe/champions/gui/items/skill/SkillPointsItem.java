package com.bindothorpe.champions.gui.items.skill;

import com.bindothorpe.champions.DomainController;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SkillPointsItem extends GuiItem {

    private final String buildId;
    private final DomainController dc;
    private int skillPoints;
    public SkillPointsItem(String buildId, DomainController dc) {
        super(new ItemStack(Material.AMETHYST_SHARD));
        this.dc = dc;
        this.buildId = buildId;
        this.skillPoints = dc.getBuildManager().getSkillPointsFromBuild(buildId);

        initialize();
    }

    private void initialize() {
        getItem().setAmount(skillPoints);
        ItemMeta meta = getItem().getItemMeta();
        meta.displayName(Component.text("Skill Points").color(NamedTextColor.LIGHT_PURPLE));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));
        lore.add(Component.text("Skill Points are used to level up your skills").color(NamedTextColor.GRAY));

        meta.lore(lore);

        getItem().setItemMeta(meta);
    }
}
