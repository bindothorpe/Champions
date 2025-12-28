package com.bindothorpe.champions.gui.map.details;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChestGameObject;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BackItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.map.MenuChestsItem;
import com.bindothorpe.champions.gui.items.map.edit.ChestInstanceItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ListChestsMapGui extends PlayerGui {
    private static final int WIDTH = 9;
    private static final int HEIGHT = 3;
    public static final int MAX_CHESTS = 7;

    private final GameMap gameMap;

    private StaticPane rootPane;

    public ListChestsMapGui(UUID uuid, DomainController dc, GameMap gameMap) {
        super(uuid, dc);
        this.gameMap = gameMap;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, "Chests");

        // Create the StaticPane without casting
        rootPane = new StaticPane(0, 0, WIDTH, HEIGHT, Pane.Priority.HIGH);

        // Create the border separately (it returns an OutlinePane)
        Pane border = OutlinePane.createBorder(0, 0, WIDTH, HEIGHT, new BorderItem());

        rootPane.addItem(new BackItem(event -> dc.getGuiManager().openEditMapGui(event.getWhoClicked().getUniqueId(), dc, gameMap)), 0, 0);
        rootPane.addItem(new MenuChestsItem(dc, true, gameMap, uuid -> new ListChestsMapGui(uuid, dc, gameMap).open()), Slot.fromXY(4, 0));

        // Add both panes to the GUI
        gui.addPane(border);
        gui.addPane(rootPane);

        AtomicInteger counter = new AtomicInteger(1);

        gameMap.getGameObjects().stream()
                .filter((gameObject -> gameObject instanceof ChestGameObject))
                .limit(MAX_CHESTS)
                .forEach(gameObject -> rootPane.addItem(new ChestInstanceItem(dc, (ChestGameObject) gameObject, gameMap), counter.getAndIncrement(), 1));
    }

}
