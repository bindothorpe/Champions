package com.bindothorpe.champions.gui;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.gui.build.BuildsOverviewGui;
import com.bindothorpe.champions.gui.build.ClassOverviewGui;
import com.bindothorpe.champions.gui.build.EditBuildGui;

import java.util.UUID;

public class GuiManager {

    private static GuiManager instance;

    private DomainController dc;

    private GuiManager(DomainController dc) {
        this.dc = dc;
    }

    public static GuiManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new GuiManager(dc);
        }
        return instance;
    }

    public void openBuildsOverviewGui(UUID uuid, ClassType classType) {
        new BuildsOverviewGui(uuid, classType, dc).open();
    }

    public void openClassOverviewGui(UUID uuid) {
        new ClassOverviewGui(uuid, dc).open();
    }

    public void openEditBuildGui(UUID uuid, String buildId, int buildNumber) {
        new EditBuildGui(uuid, buildId, buildNumber, dc).open();
    }
}
