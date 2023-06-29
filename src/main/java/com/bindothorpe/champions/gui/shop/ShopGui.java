package com.bindothorpe.champions.gui.shop;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.customItem.CustomItemItem;
import com.bindothorpe.champions.gui.items.customItem.PathItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.List;
import java.util.UUID;

public class ShopGui extends PlayerGui {

    private final CustomItemId customItemId;
    private static final int WIDTH = 9;
    private static final int HEIGHT = 6;

    public ShopGui(UUID uuid, CustomItemId customItemId, DomainController dc) {
        super(uuid, dc);
        this.customItemId = customItemId;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, "Shop");

        StaticPane root = new StaticPane(0, 0, WIDTH, HEIGHT);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                root.addItem(new BorderItem(), x, y);
            }
        }


        CustomItem customItem = dc.getCustomItemManager().getCustomItem(customItemId);
        if (customItem != null) {
            placeItemInGui(root, customItem, true, WIDTH / 2, 0, 0, dc.getCustomItemManager().doesUserHaveItem(uuid, customItemId));
        }

        gui.addPane(root);
    }

    private void placeItemInGui(StaticPane root, CustomItem item, boolean isRoot, int x, int y, int direction, boolean isUnlocked) {
        if (isRoot) {
            y = 3 - item.getTier();
        }

        root.addItem(new CustomItemItem(item), x, y);

        List<CustomItemId> subItems = dc.getCustomItemManager().getSubItems(item.getId());

        if (subItems.isEmpty()) return;

        int subItemsCount = subItems.size();
        int tier = item.getTier();


        for (int i = 0; i < subItemsCount; i++) {
            CustomItemId id = subItems.get(i);
            boolean unlocked = isUnlocked || dc.getCustomItemManager().doesUserHaveItem(uuid, id);
            int horizontalModifier = i * 2 - 1;
            int xOffset = 0;
            int yOffset = 2;


            if (!(isRoot && subItemsCount != 1))
                root.addItem(new PathItem(unlocked), x, y + 1);

            if (subItemsCount == 2) {
                xOffset = 2 * horizontalModifier;

                if (isRoot || direction == horizontalModifier)
                    root.addItem(new PathItem(unlocked), x + xOffset + (horizontalModifier * -1), y);


                if (tier == 2 && direction == -1 && i == 1) {
                    xOffset = 0;
                } else if (tier == 2 && direction == 1 && i == 0) {
                    xOffset = 0;
                } else {
                    root.addItem(new PathItem(unlocked), x + xOffset, y + 1);
                    root.addItem(new PathItem(unlocked), x + xOffset, y);
                }

            }

            CustomItem subItem = dc.getCustomItemManager().getCustomItem(subItems.get(i));
            placeItemInGui(root, subItem, false, x + xOffset, y + yOffset, horizontalModifier, unlocked);
        }

    }
}

