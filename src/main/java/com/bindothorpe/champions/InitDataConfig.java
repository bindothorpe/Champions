package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.skill.skills.KitingArrow;
import com.bindothorpe.champions.domain.skill.skills.TestSkill;
import com.bindothorpe.champions.domain.skill.skills.TestSkill2;

public class InitDataConfig {

    private final DomainController dc;

    public InitDataConfig(DomainController dc) {
        this.dc = dc;
    }

    public void initialize() {
        dc.registerSkill(new TestSkill(dc));
        dc.registerSkill(new TestSkill2(dc));
        dc.registerSkill(new KitingArrow(dc));
    }
}
