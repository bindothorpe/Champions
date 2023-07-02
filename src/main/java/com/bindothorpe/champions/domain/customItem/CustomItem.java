package com.bindothorpe.champions.domain.customItem;

import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.IntUtil;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CustomItem implements Listener {

    private static Set<UUID> users = new HashSet<>();
    private final CustomItemManager manager;
    private final CustomItemId id;
    private final Set<CustomItemType> types;
    private final String name;
    private final Material material;
    private final int upgradePrice;
    private final List<CustomItemId> subItems;
    private final List<EntityStatus> statuses;

    public CustomItem(CustomItemManager manager, CustomItemId id, Set<CustomItemType> types, String name, Material material, int upgradePrice, List<CustomItemId> subItems) {
        this.manager = manager;
        this.id = id;
        this.types = types;
        this.name = name;
        this.material = material;
        this.upgradePrice = upgradePrice;
        this.subItems = subItems;
        this.statuses = new ArrayList<>();
    }

    public CustomItem(CustomItemManager manager, CustomItemId id, Set<CustomItemType> types, String name, Material material, int upgradePrice) {
        this(manager, id, types, name, material, upgradePrice, List.of());
    }

    public void addUser(UUID uuid) {
        users.add(uuid);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public CustomItemId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getUpgradePrice() {
        return upgradePrice;
    }

    public int getTotalPrice() {
        return subItems.stream().map(manager::getTotalPrice).reduce(upgradePrice, Integer::sum);
    }

    public List<CustomItemId> getSubItems() {
        return subItems;
    }

    public List<EntityStatus> getStatuses() {
        return statuses;
    }

    public int getTier() {
        int tier = 0;

        for (CustomItemId subItem : subItems) {
            tier = Math.max(tier, manager.getTier(subItem));
        }

        return tier + 1;
    }

    public ItemStack getItem(UUID uuid) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(getItemName());
        meta.lore(getLore(uuid));
        meta.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        item.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);

        return item;
    }


    public ItemStack getItem() {
        return getItem(null);
    }

    public Set<CustomItemType> getTypes() {
        return types;
    }

    public boolean isType(CustomItemType type) {
        return types.contains(type);
    }

    protected Component getItemName() {
        Component c = Component.text(name);
        switch (getTier()) {
            case 1:
                c = c.color(getColor()).decorate(TextDecoration.BOLD);
                break;
            case 2:
                c = c.color(getColor()).decorate(TextDecoration.BOLD);
                break;
            case 3:
                c = c.color(getColor()).decorate(TextDecoration.BOLD);
                break;
        }

        return c;
    }

    protected NamedTextColor getColor() {
        switch (getTier()) {
            case 1:
                return NamedTextColor.GRAY;
            case 2:
                return NamedTextColor.GOLD;
            case 3:
                return NamedTextColor.RED;
        }

        return NamedTextColor.WHITE;
    }

    protected List<Component> getLore(UUID uuid) {
        List<Component> lore = new ArrayList<>();

        if (uuid != null) {
            Component types = Component.text("Type: ").color(NamedTextColor.GRAY);

            List<CustomItemType> sortedTypes = getTypes().stream().sorted(Comparator.comparingInt(CustomItemType::ordinal)).collect(Collectors.toList());

            for (int i = 0; i < getTypes().size(); i++) {
                CustomItemType type = sortedTypes.get(i);
                types = types.append(Component.text(TextUtil.camelCasing(type.toString().replace("_", " "))).color(type.getColor()));

                if (i < getTypes().size() - 1)
                    types = types.append(Component.text(", ").color(NamedTextColor.GRAY));
            }

            lore.add(types);
        } else {
            lore.add(Component.text("Tier: ").color(NamedTextColor.GRAY)
                    .append(Component.text(IntUtil.toRoman(getTier())).color(getColor())));
        }

        lore.add(Component.text(" "));
        for (EntityStatus status : statuses) {
            lore.add(Component.text(TextUtil.camelCasing(status.getType().toString().replace("_", " "))).color(NamedTextColor.GRAY)
                    .append(Component.text(": ").color(NamedTextColor.GRAY))
                    .append(Component.text(status.getValue() < 0 ? "" : "+").color(NamedTextColor.YELLOW))
                    .append(Component.text(status.isMultiplier() ? status.getValue() * 100 : status.getValue()).color(NamedTextColor.YELLOW))
                    .append(status.isMultiplier() ? Component.text("%").color(NamedTextColor.YELLOW) : Component.empty()));
        }

        List<Component> additionalLore = getAdditionalLore();

        if(!additionalLore.isEmpty()) {
            lore.addAll(additionalLore);
            lore.add(Component.text(" "));
        }

        if (!statuses.isEmpty())
            lore.add(Component.text(" "));

        lore.add(Component.text("Price: ").color(NamedTextColor.GRAY)
                .append(Component.text(uuid == null ? getTotalPrice() : manager.getRemainingCost(uuid, id)).color(NamedTextColor.YELLOW)
                        .append(Component.text(" gold"))));

        if (uuid != null)
            lore.add(ComponentUtil.leftClick(true)
                    .append(Component.text("to purchase").color(NamedTextColor.GRAY)));

        return lore;
    }

    protected List<Component> getAdditionalLore() {
        return new ArrayList<>();
    }

}
