package com.bindothorpe.champions.gui.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.map.*;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.UUID;

public class EditMapGui extends PlayerGui {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 3;

    private final GameMap gameMap;

    public EditMapGui(UUID uuid, DomainController dc, GameMap gameMap) {
        super(uuid, dc);
        this.gameMap = gameMap;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, String.format("Edit %s", gameMap.getName()));

        StaticPane root = new StaticPane(0, 0, WIDTH, HEIGHT);

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                root.addItem(new BorderItem(), x, y);
            }
        }

        root.addItem(new MapItem(dc, gameMap), 4, 0);
        root.addItem(new MenuCapturePointsItem(dc, gameMap), 2, 1);
        root.addItem(new MenuSpawnPointsItem(dc, gameMap), 4, 1);
        root.addItem(new MenuBuildSelectsItem(dc, gameMap), 6, 1);
        root.addItem(new MenuGemsItem(dc, gameMap), 3, 2);
        root.addItem(new MenuChestsItem(dc, gameMap), 5, 2);

        gui.addPane(root);
    }
}
