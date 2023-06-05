package com.bindothorpe.champions.domain;

import com.bindothorpe.champions.DomainController;

import java.util.Map;

public class BuildManager {

    private DomainController dc;
    private static BuildManager instance;
    private Map<String, Build> builds;

    private BuildManager(DomainController dc) {
        this.dc = dc;
    }

    public static BuildManager getInstance(DomainController dc) {
        if(instance == null)
            instance = new BuildManager(dc);

        return instance;
    }



}
