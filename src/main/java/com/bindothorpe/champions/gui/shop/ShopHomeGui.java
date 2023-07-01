package com.bindothorpe.champions.gui.shop;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.customItem.ShopItem;
import com.bindothorpe.champions.gui.items.customItem.ShopNavigationItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Orientable;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopHomeGui extends PlayerGui {

    private final CustomItemType type;
    private static final int WIDTH = 9;
    private static final int HEIGHT = 4;

    public ShopHomeGui(UUID uuid, DomainController dc, CustomItemType type) {
        super(uuid, dc);
        this.type = type;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, "Shop");

        StaticPane root = new StaticPane(0, 0, WIDTH, HEIGHT);

        for (int x = 0; x < WIDTH; x++) {
            root.addItem(new BorderItem(), x, 0);
        }

        for(CustomItemType type : CustomItemType.values()) {
            root.addItem(new ShopNavigationItem(type, dc), type.ordinal() + 3, 0);
        }

        OutlinePane outlinePane = new OutlinePane(0, 1, WIDTH, HEIGHT - 1);
        outlinePane.setOrientation(Orientable.Orientation.HORIZONTAL);

        Arrays.stream(CustomItemId.values())
                .map(dc.getCustomItemManager()::getCustomItem)
                .sorted((c1, c2) -> Integer.compare(c2.getTier(), c1.getTier()))
                .filter(item -> item.isType(type))
                .forEach(c -> outlinePane.addItem(new ShopItem(c, dc, type)));


        gui.addPane(root);
        gui.addPane(outlinePane);

    }
}
