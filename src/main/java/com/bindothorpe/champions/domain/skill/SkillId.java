package com.bindothorpe.champions.domain.skill;

public enum SkillId {
    TEST_SKILL(SkillType.SWORD),
    TEST_SKILL_2(SkillType.SWORD),
    KITING_ARROW(SkillType.BOW),
    BOUNCING_ARROW(SkillType.PASSIVE_B),
    EXPLOSION(SkillType.AXE),
    ASSASSIN_PASSIVE(SkillType.CLASS_PASSIVE),
    ICE_PRISON(SkillType.AXE),
    HUNTERS_HEART(SkillType.PASSIVE_B),
    EXPLOSIVE_BOMB(SkillType.AXE),
    HEAD_BUTT(SkillType.AXE),
    RALLY(SkillType.SWORD),
    SONAR_ARROW(SkillType.BOW),
    GRAND_ENTRANCE(SkillType.AXE),
    MAGE_PASSIVE(SkillType.CLASS_PASSIVE),
    LEAP(SkillType.AXE),
    MUSIC(SkillType.SWORD),
    WOLFS_POUNCE(SkillType.SWORD),
    MAGNETIC_PULL(SkillType.PASSIVE_A),
    TRAMPLE(SkillType.PASSIVE_A),
    HEAVY_SWING(SkillType.SWORD),
    SILK_ARROW(SkillType.BOW),
    SMOKE_ARROW(SkillType.BOW),
    BULLS_CHARGE(SkillType.AXE),
    FLESH_HOOK(SkillType.SWORD);


    private final SkillType skillType;

    SkillId(SkillType skillType) {
        this.skillType = skillType;
    }

    public SkillType getSkillType() {
        return skillType;
    }
}
