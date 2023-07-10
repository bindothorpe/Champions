package com.bindothorpe.champions.gui.map;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.map.MapItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.List;
import java.util.UUID;

public class MapMainGui extends PlayerGui {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 6;

    public MapMainGui(UUID uuid, DomainController dc) {
        super(uuid, dc);
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(HEIGHT, "Game Maps");

        StaticPane root = new StaticPane(0, 0, WIDTH, HEIGHT);

        //Add a border around the gui
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (x == 0 || x == WIDTH - 1 || y == 0 || y == 1 || y == HEIGHT - 1)
                    root.addItem(new BorderItem(), x, y);
            }
        }

        PaginatedPane paginatedPane = new PaginatedPane(1, 2, 7, 3);

        //Get a list of maps
        List<String> maps = dc.getGameMapManager().getGameMapNames();
        System.out.println(maps.size());

        //Add a page for every 21 maps
        for(int i = 0; i < (maps.size() / 21) + 1; i++) {
            OutlinePane panePage = new OutlinePane(0, 0, 7, 3);

            int size = maps.size();
            List<String> subList = maps.subList(i * 21, Math.min((i + 1) * 21, size));
            System.out.println(subList.size());

            //Add a map item for every map
            for(String mapName : subList) {
                panePage.addItem(new MapItem(dc, mapName));
                System.out.println(mapName);
            }

            paginatedPane.addPage(panePage);
        }

        gui.addPane(root);
        gui.addPane(paginatedPane);
    }
}
