package com.bindothorpe.champions.gui.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BackItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.build.BuildItem;
import com.bindothorpe.champions.gui.items.build.ClassIconItem;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.Set;
import java.util.UUID;

public class BuildsOverviewGui extends PlayerGui {

    private ClassType classType;

    public BuildsOverviewGui(UUID uuid, ClassType classType, DomainController dc) {
        super(uuid, dc);
        this.classType = classType;
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(3, String.format("%s Builds", TextUtil.camelCasing(classType.toString())));
        OutlinePane buildsPane = new OutlinePane(1, 1, 7, 1);
        StaticPane root = new StaticPane(0, 0, 9, 3);
        for(int i = 0; i < 27; i++) {
            int x = i % 9;
            int y = i / 9;
            if(x == 0 || x == 8 || y == 0 || y == 2)
                root.addItem(new BorderItem(), x, y);

        }

        Set<String> buildIds = dc.getBuildIdsFromPlayer(uuid).get(classType);

        for(int i = 0; i < buildIds.size(); i++) {
            String buildId = (String) buildIds.toArray()[i];
            buildsPane.addItem(new BuildItem(buildId, i + 1, dc));
        }

        root.addItem(new BackItem(event -> dc.openClassOverviewGui(event.getWhoClicked().getUniqueId())), 0, 0);
        root.addItem(new ClassIconItem(uuid, classType, dc, true, false, true, false), 4, 0);
        gui.addPane(root);
        gui.addPane(buildsPane);
    }
}
