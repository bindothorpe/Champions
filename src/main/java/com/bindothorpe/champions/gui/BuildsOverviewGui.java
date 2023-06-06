package com.bindothorpe.champions.gui;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.gui.items.BuildItem;
import com.bindothorpe.champions.gui.items.ClassIconItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BuildsOverviewGui {

    private DomainController dc;
    private UUID uuid;

    private ChestGui gui;

    public BuildsOverviewGui(DomainController dc, UUID uuid) {
        this.dc = dc;
        this.uuid = uuid;
        initialize();
    }

    public void show() {
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) {
            System.out.println("Player is null");
            System.out.println(uuid);
            return;
        }
        gui.show(player);
    }

    private void initialize() {
        this.gui = new ChestGui(6, "Builds Overview");

        // Get all builds from the player
        Map<ClassType, Set<String>> builds = dc.getBuildIdsFromPlayer(uuid);

        // Start a counter to keep track of the x position
        int x = 0;

        // Loop through all class types except GLOBAL
        for(ClassType classType : ClassType.values()) {
            if(classType.equals(ClassType.GLOBAL)) {
                continue;
            }

            // Create a new pane for the class type
            OutlinePane classPane = new OutlinePane(x, 1, 1, 4);

            // Add the class icon to the pane
            classPane.addItem(new ClassIconItem(uuid, classType, dc));
            System.out.println("Added class icon for " + classType.name());

            // Start a counter to keep track of the build number
            int counter = 1;

            // Loop through all builds for the class type
            for(String buildId : builds.get(classType)) {

                // Add the build item to the pane
                classPane.addItem(new BuildItem(buildId, counter++, dc));

            }

            // Add the class pane to the root pane
            gui.addPane(classPane);
            x += 2;
        }
    }
}
