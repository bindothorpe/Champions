package com.bindothorpe.champions.gui.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.build.ClassIconItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.Arrays;
import java.util.UUID;

public class ClassOverviewGui extends PlayerGui {
    public ClassOverviewGui(UUID uuid, DomainController dc) {
        super(uuid, dc);
        initialize();
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(1, "Class Overview");

        OutlinePane classes = new OutlinePane(2, 0, 5, 1);
        StaticPane root = new StaticPane(0, 0, 9, 1);

        for(int i = 0; i < 9; i++) {
            if(i < 2 || i > 6)
                root.addItem(new BorderItem(), i, 0);
        }

        Arrays.stream(ClassType.values()).filter(c -> c != ClassType.GLOBAL).forEach(c -> {
            classes.addItem(new ClassIconItem(uuid, c, dc, false, true, false, true));
        });

        gui.addPane(root);
        gui.addPane(classes);
    }


}
