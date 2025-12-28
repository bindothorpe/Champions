package com.bindothorpe.champions.gui.map.details;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.game.map.gameObjects.SpawnPointGameObject;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BackItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.map.edit.spawnPoint.SpawnPointInstanceItem;
import com.bindothorpe.champions.gui.items.map.edit.spawnPoint.SpawnPointTeamItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ListSpawnPointsMapGui extends PlayerGui {
    private static final int WIDTH = 9;
    private static final int HEIGHT = 6;
    public static final int MAX_SPAWN_POINTS_PER_TEAM = 12;

    private final GameMap gameMap;

    public ListSpawnPointsMapGui(UUID uuid, DomainController dc, GameMap gameMap) {
        super(uuid, dc);
        this.gameMap = gameMap;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, "Spawn Points");

        // Create the StaticPane without casting
        StaticPane rootPane = new StaticPane(0, 0, WIDTH, HEIGHT, Pane.Priority.HIGH);

        // Create the border separately (it returns an OutlinePane)
        Pane border = OutlinePane.createBorder(0, 0, WIDTH, HEIGHT, new BorderItem());
        StaticPane centerDivider = new StaticPane(4, 0, 1, HEIGHT, Pane.Priority.LOW);
        for (int y = 0; y < HEIGHT; y++) {
            centerDivider.addItem(new BorderItem(), 0, y);
        }

        rootPane.addItem(new BackItem(event -> dc.getGuiManager().openEditMapGui(event.getWhoClicked().getUniqueId(), dc, gameMap)), 0, 0);

        // Add team items for BLUE and RED
        rootPane.addItem(new SpawnPointTeamItem(dc, TeamColor.BLUE, gameMap, uuid -> new ListSpawnPointsMapGui(uuid, dc, gameMap).open()), Slot.fromXY(2, 0));
        rootPane.addItem(new SpawnPointTeamItem(dc, TeamColor.RED, gameMap, uuid -> new ListSpawnPointsMapGui(uuid, dc, gameMap).open()), Slot.fromXY(6, 0));

        // Add all panes to the GUI
        gui.addPane(border);
        gui.addPane(centerDivider);
        gui.addPane(rootPane);

        // Create blue team subframe (3 wide, 4 high, starting at 1,1)
        StaticPane bluePane = new StaticPane(1, 1, 3, 4, Pane.Priority.NORMAL);
        AtomicInteger blueCounter = new AtomicInteger(0);
        gameMap.getGameObjectsOfType(GameObjectType.SPAWN_POINT).stream()
                .filter(gameObject -> ((SpawnPointGameObject) gameObject).team().equals(TeamColor.BLUE))
                .limit(MAX_SPAWN_POINTS_PER_TEAM)
                .forEach(gameObject -> {
                    int index = blueCounter.getAndIncrement();
                    int x = index % 3;
                    int y = index / 3;
                    bluePane.addItem(new SpawnPointInstanceItem(dc, (SpawnPointGameObject) gameObject, gameMap), x, y);
                });
        gui.addPane(bluePane);

        // Create red team subframe (3 wide, 4 high, starting at 5,1)
        StaticPane redPane = new StaticPane(5, 1, 3, 4, Pane.Priority.NORMAL);
        AtomicInteger redCounter = new AtomicInteger(0);
        gameMap.getGameObjectsOfType(GameObjectType.SPAWN_POINT).stream()
                .filter(gameObject -> ((SpawnPointGameObject) gameObject).team().equals(TeamColor.RED))
                .limit(MAX_SPAWN_POINTS_PER_TEAM)
                .forEach(gameObject -> {
                    int index = redCounter.getAndIncrement();
                    int x = index % 3;
                    int y = index / 3;
                    redPane.addItem(new SpawnPointInstanceItem(dc, (SpawnPointGameObject) gameObject, gameMap), x, y);
                });
        gui.addPane(redPane);
    }

}