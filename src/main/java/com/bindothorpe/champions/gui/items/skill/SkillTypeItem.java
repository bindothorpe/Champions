package com.bindothorpe.champions.gui.items.skill;

import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SkillTypeItem extends GuiItem {

    private final SkillType skillType;
    public SkillTypeItem(SkillType skillType) {
        super(new ItemStack(Material.IRON_SWORD));
        this.skillType = skillType;
        initialize();
    }

    private void initialize() {
        getItem().setType(getSkillTypeMaterial());
        getItem().addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        ItemMeta meta = getItem().getItemMeta();
        meta.displayName(Component.text(TextUtil.camelCasing(skillType.toString())).color(NamedTextColor.WHITE));
        getItem().setItemMeta(meta);
    }

    private Material getSkillTypeMaterial() {
        return switch (skillType) {
            case SWORD -> Material.IRON_SWORD;
            case AXE -> Material.IRON_AXE;
            case BOW -> Material.BOW;
            case PASSIVE_A -> Material.RED_DYE;
            case PASSIVE_B -> Material.ORANGE_DYE;
            default -> Material.YELLOW_DYE;
        };
    }
}
