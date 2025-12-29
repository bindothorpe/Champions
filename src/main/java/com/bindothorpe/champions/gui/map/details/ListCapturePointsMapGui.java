package com.bindothorpe.champions.gui.map.details;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.gameObjects.CapturePointGameObject;
import com.bindothorpe.champions.domain.game.map.gameObjects.ChampionSelectGameObject;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BackItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.map.MenuCapturePointsItem;
import com.bindothorpe.champions.gui.items.map.MenuChampionSelectsItem;
import com.bindothorpe.champions.gui.items.map.edit.CapturePointInstanceItem;
import com.bindothorpe.champions.gui.items.map.edit.ChampionSelectInstanceItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ListCapturePointsMapGui extends PlayerGui {
    private static final int WIDTH = 9;
    private static final int HEIGHT = 3;
    public static final int MAX_CAPTURE_POINTS = 7;

    private final GameMap gameMap;

    private StaticPane rootPane;

    public ListCapturePointsMapGui(UUID uuid, DomainController dc, GameMap gameMap) {
        super(uuid, dc);
        this.gameMap = gameMap;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, "Capture Points");

        // Create the StaticPane without casting
        rootPane = new StaticPane(0, 0, WIDTH, HEIGHT, Pane.Priority.HIGH);

        // Create the border separately (it returns an OutlinePane)
        Pane border = OutlinePane.createBorder(0, 0, WIDTH, HEIGHT, new BorderItem());

        rootPane.addItem(new BackItem(event -> dc.getGuiManager().openEditMapGui(event.getWhoClicked().getUniqueId(), dc, gameMap)), 0, 0);
        rootPane.addItem(new MenuCapturePointsItem(dc, true, gameMap, uuid -> new ListCapturePointsMapGui(uuid, dc, gameMap).open()), Slot.fromXY(4, 0));

        // Add both panes to the GUI
        gui.addPane(border);
        gui.addPane(rootPane);

        AtomicInteger counter = new AtomicInteger(1);

        gameMap.getGameObjects().stream()
                .filter((gameObject -> gameObject instanceof CapturePointGameObject))
                .limit(MAX_CAPTURE_POINTS)
                .forEach(gameObject -> rootPane.addItem(new CapturePointInstanceItem(dc, (CapturePointGameObject) gameObject, gameMap), counter.get() % MAX_CAPTURE_POINTS, 1));
    }

}
