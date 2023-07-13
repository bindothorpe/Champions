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
    LEAP(SkillType.AXE), MUSIC(SkillType.SWORD), WOLFS_POUNCE(SkillType.SWORD);

    private final SkillType skillType;

    SkillId(SkillType skillType) {
        this.skillType = skillType;
    }

    public SkillType getSkillType() {
        return skillType;
    }
}
