package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public class Spellblade extends CustomItem {
    public Spellblade(CustomItemManager manager) {
        super(manager, CustomItemId.SPELLBLADE, Set.of(CustomItemType.ATTACK), "Spellblade", Material.GOLDEN_SWORD, 400, List.of(CustomItemId.LONG_SWORD, CustomItemId.LONG_SWORD));
    }

    @Override
    protected List<Component> getAdditionalLore() {
        List<Component> lore = super.getAdditionalLore();
        lore.add(ComponentUtil.passive()
                .append(Component.text("Every time you").color(NamedTextColor.GRAY)));
        lore.add(Component.text("cast a spell, your next").color(NamedTextColor.GRAY));
        lore.add(Component.text("attack deals bonus damage").color(NamedTextColor.GRAY));

        return lore;
    }
}
