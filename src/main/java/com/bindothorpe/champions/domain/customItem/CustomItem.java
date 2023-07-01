package com.bindothorpe.champions.domain.customItem;

import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.util.ComponentUtil;
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

public abstract class CustomItem implements Listener {

    private static Set<UUID> users = new HashSet<>();
    private final CustomItemManager manager;
    private final CustomItemId id;
    private final String name;
    private final Material material;
    private final int upgradePrice;
    private final List<CustomItemId> subItems;
    private final List<EntityStatus> statuses;

    public CustomItem(CustomItemManager manager, CustomItemId id, String name, Material material, int upgradePrice, List<CustomItemId> subItems, List<EntityStatus> statuses) {
        this.manager = manager;
        this.id = id;
        this.name = name;
        this.material = material;
        this.upgradePrice = upgradePrice;
        this.subItems = subItems;
        this.statuses = statuses;
    }

    public CustomItem(CustomItemManager manager, CustomItemId id, String name, Material material, int upgradePrice, List<EntityStatus> statuses) {
        this(manager, id, name, material, upgradePrice, List.of(), statuses);
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

    protected Component getItemName() {
        Component c = Component.text(name);
        switch (getTier()) {
            case 1:
                c = c.color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
                break;
            case 2:
                c = c.color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
                break;
            case 3:
                c = c.color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
                break;
        }

        return c;
    }

    protected List<Component> getLore(UUID uuid) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.text(" "));
        for (EntityStatus status : statuses) {
            lore.add(Component.text(TextUtil.camelCasing(status.getType().toString().replace("_", " "))).color(NamedTextColor.GRAY)
                    .append(Component.text(": ").color(NamedTextColor.GRAY))
                    .append(Component.text(status.getValue() < 0 ? "" : "+").color(NamedTextColor.YELLOW))
                    .append(Component.text(status.getValue()).color(NamedTextColor.YELLOW))
                    .append(status.isMultiplier() ? Component.text("%").color(NamedTextColor.YELLOW) : Component.empty()));
        }

        if(!statuses.isEmpty())
        lore.add(Component.text(" "));

        lore.add(Component.text("Price: ").color(NamedTextColor.GRAY)
                .append(Component.text(manager.getRemainingCost(uuid, id)).color(NamedTextColor.YELLOW)
                        .append(Component.text(" gold"))));

        lore.add(ComponentUtil.leftClick(true)
                .append(Component.text("to purchase").color(NamedTextColor.GRAY)));

        return lore;
    }

}
