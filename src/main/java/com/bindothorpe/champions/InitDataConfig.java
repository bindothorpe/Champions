package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.skill.skills.TestSkill;
import com.bindothorpe.champions.domain.skill.skills.TestSkill2;

public class InitDataConfig {

    private final DomainController dc;

    public InitDataConfig(DomainController dc) {
        this.dc = dc;
    }

    public void initialize() {
        dc.registerSkill(new TestSkill());
        dc.registerSkill(new TestSkill2());
    }
}
