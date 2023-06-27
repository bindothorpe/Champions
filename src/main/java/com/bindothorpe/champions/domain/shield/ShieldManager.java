package com.bindothorpe.champions.domain.shield;

import com.bindothorpe.champions.DomainController;
import org.bukkit.Bukkit;

public class ShieldManager {

    private static ShieldManager instance;
    private DomainController dc;

    private ShieldManager(DomainController dc) {
        this.dc = dc;
    }

    public static ShieldManager getInstance(DomainController dc) {
        if (instance == null) {
            instance = new ShieldManager(dc);
        }
        return instance;
    }

    public DomainController getDomainController() {
        return dc;
    }
}
