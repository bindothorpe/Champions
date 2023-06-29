package com.bindothorpe.champions.gui.shop;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.customItem.CustomItemItem;
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

        CustomItem customItem = dc.getCustomItemManager().getCustomItem(customItemId);
        if (customItem != null) {
            placeItemInGui(root, customItem, true, WIDTH / 2, 0, 0);
        }

        gui.addPane(root);
    }

    private void placeItemInGui(StaticPane root, CustomItem item, boolean isRoot, int x, int y, int direction) {
        if (isRoot) {
            y = 3 - item.getTier();
        }

        root.addItem(new CustomItemItem(item), x, y);

        List<CustomItemId> subItems = dc.getCustomItemManager().getSubItems(item.getId());

        if (subItems.isEmpty()) return;

        int subItemsCount = subItems.size();
        int tier = item.getTier();

        if (!(isRoot && subItemsCount != 1))
            root.addItem(new BorderItem(), x, y + 1);


        for (int i = 0; i < subItemsCount; i++) {
            int horizontalModifier = i * 2 - 1;
            int xOffset = 0;
            int yOffset = 2;

            if (subItemsCount == 2) {
                xOffset = 2 * horizontalModifier;

                if (isRoot || direction == horizontalModifier)
                    root.addItem(new BorderItem(), x + xOffset + (horizontalModifier * -1), y);


                if (tier == 2 && direction == -1 && i == 1) {
                    xOffset = 0;
                } else if (tier == 2 && direction == 1 && i == 0) {
                    xOffset = 0;
                } else {
                    root.addItem(new BorderItem(), x + xOffset, y + 1);
                    root.addItem(new BorderItem(), x + xOffset, y);
                }

            }

            CustomItem subItem = dc.getCustomItemManager().getCustomItem(subItems.get(i));
            placeItemInGui(root, subItem, false, x + xOffset, y + yOffset, horizontalModifier);
        }

    }
}

//    // Recursively add items to GUI
//    private void addItemsToPane(StaticPane root, CustomItemId itemId, int x, int y) {
//        // Display the item
//        CustomItem item = dc.getCustomItemManager().getCustomItem(itemId);
//        root.addItem(new CustomItemItem(item), x, y);
//
//        // Display sub-items
//        List<CustomItemId> subItems = dc.getCustomItemManager().getSubItems(itemId);
//        int subItemCount = subItems.size();
//
//        if (subItemCount > 0) {
//            // Calculate positions for sub-items
//            int startX = x - (subItemCount - 1);
//            if (startX < 0) startX = 0;
//
//            for (int i = 0; i < subItemCount; i++) {
//                int newX = startX + 2 * i;
//                int newY = y + 2;
//
//                // Recursively add sub-items to GUI
//                addItemsToPane(root, subItems.get(i), newX, newY);
//            }
//
//            // Add connecting lines between items
//            for (int i = 0; i < subItemCount; i++) {
//                addConnectingLines(root, x, y + 1, startX + 2 * i, y + 2 - 1);
//            }
//        }
//    }
//
//    // Add connecting lines between items
//    private void addConnectingLines(StaticPane root, int startX, int startY, int endX, int endY) {
//        if (startX == endX) {
//            // Add vertical line
//            for (int y = startY; y <= endY; y++) {
//                root.addItem(new BorderItem(), startX, y);
//            }
//        } else {
//            // Add horizontal line
//            for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
//                root.addItem(new BorderItem(), x, startY);
//            }
//        }
//    }
//
//    // Initialize GUI
//    @Override
//    protected void initialize() {
//        gui = new ChestGui(6, "Shop");
//
//        StaticPane root = new StaticPane(0, 0, 9, 6);
//
//        addItemsToPane(root, customItemId, 4, 0);
//
//        gui.addPane(root);
//    }


//    @Override
//    protected void initialize() {
//        gui = new ChestGui(6, "Shop");
//
//        StaticPane root = new StaticPane(0, 0, 9, 6);
//
//        CustomItem customItem = dc.getCustomItemManager().getCustomItem(customItemId);
//
//        if(customItem != null) {
//            int itemTier = dc.getCustomItemManager().getTier(customItemId);
//            int y = 3 - itemTier;
//            int x = 4;
//
//            root.addItem(new CustomItemItem(customItem), x, y);
//            List<CustomItemId> subItems = dc.getCustomItemManager().getSubItems(customItemId);
//
//            for(int i = 0; i < subItems.size(); i++) {
//                CustomItemId subItemId = subItems.get(i);
//                int iDirection = i * 2 - 1;
//                int xi = x + iDirection * 2;
//                root.addItem(new CustomItemItem(dc.getCustomItemManager().getCustomItem(subItemId)), xi, y + 2);
//
//                List<CustomItemId> subSubItems = dc.getCustomItemManager().getSubItems(subItemId);
//
//                for(int j = 0; j < subSubItems.size(); j++) {
//                    CustomItemId subSubItemId = subSubItems.get(j);
//                    int xj = xi;
//                    int yj = y + 4;
//                    if(j == 0) {
//                        yj = y + 5;
//                    } else if(j == 1) {
//                        xj = xi + 2 * iDirection;
//                    }
//                    root.addItem(new CustomItemItem(dc.getCustomItemManager().getCustomItem(subSubItemId)), xj, yj);
//                }
//
//            }
//
//        }
//
//
//
//        gui.addPane(root);
//
//    }

