package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.skill.skills.TestSkill;

public class InitDataConfig {

    private final DomainController dc;

    public InitDataConfig(DomainController dc) {
        this.dc = dc;
    }

    public void initialize() {
        dc.registerSkill(new TestSkill());
    }
}
