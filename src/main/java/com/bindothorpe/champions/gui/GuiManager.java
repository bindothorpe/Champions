package com.bindothorpe.champions.gui;

import com.bindothorpe.champions.DomainController;

import java.util.UUID;

public class GuiManager {

    private static GuiManager instance;

    private DomainController dc;

    private GuiManager(DomainController dc) {
        this.dc = dc;
    }

    public static GuiManager getInstance(DomainController dc) {
        if(instance == null)
            instance = new GuiManager(dc);
        return instance;
    }

    public void openBuildsOverviewGui(UUID uuid) {
        new BuildsOverviewGui(dc, uuid).show();
    }

    public void openEditBuildGui(UUID uuid, String buildId) {
        //TODO: implement
    }
}
