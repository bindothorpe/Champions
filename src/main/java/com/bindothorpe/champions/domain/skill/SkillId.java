package com.bindothorpe.champions.domain.skill;

public enum SkillId {
    KITING_ARROW(SkillType.BOW),
    BOUNCING_ARROW(SkillType.PASSIVE_B),
    EXPLOSION(SkillType.AXE),
    ASSASSIN_PASSIVE(SkillType.CLASS_PASSIVE),
    ICE_PRISON(SkillType.AXE),
    HUNTERS_HEART(SkillType.PASSIVE_B),
    EXPLOSIVE_BOMB(SkillType.AXE),
    HEAD_BUTT(SkillType.AXE),
    SONAR_ARROW(SkillType.BOW),
    GRAND_ENTRANCE(SkillType.AXE),
    MAGE_PASSIVE(SkillType.CLASS_PASSIVE),
    LEAP(SkillType.AXE),
    WOLFS_POUNCE(SkillType.SWORD),
    MAGNETIC_PULL(SkillType.PASSIVE_A),
    TRAMPLE(SkillType.PASSIVE_A),
    HEAVY_SWING(SkillType.SWORD),
    SILK_ARROW(SkillType.BOW),
    SMOKE_ARROW(SkillType.BOW),
    BULLS_CHARGE(SkillType.AXE),
    FLESH_HOOK(SkillType.SWORD),
    RIPOSTE(SkillType.SWORD),
    INFERNO(SkillType.SWORD),
    BLIZZARD(SkillType.SWORD),
    IMMOLATE(SkillType.PASSIVE_A),
    VOID(SkillType.PASSIVE_A),
    STATIC_LAZER(SkillType.SWORD),
    EVADE(SkillType.SWORD),
    BACK_STAB(SkillType.PASSIVE_B),
    ROPED_ARROW(SkillType.BOW),
    RECALL(SkillType.PASSIVE_A),
    SMOKE_BOMB(SkillType.PASSIVE_A),
    DEFENSIVE_STANCE(SkillType.SWORD),
    HOLD_POSITION(SkillType.AXE),
    CLEAVE(SkillType.PASSIVE_A),
    BLINK(SkillType.AXE),
    FLASH(SkillType.AXE),
    CLAW(SkillType.SWORD),
    QUICK_STEP(SkillType.PASSIVE_B), ISOLATION(SkillType.PASSIVE_B), STAMPEDE(SkillType.PASSIVE_B), LOTUS_TRAP(SkillType.PASSIVE_A);


    private final SkillType skillType;

    SkillId(SkillType skillType) {
        this.skillType = skillType;
    }

    public SkillType getSkillType() {
        return skillType;
    }
}
