package com.bindothorpe.champions.events.damage;

public class DamageModification {

    private boolean mult;
    private double value;

    public DamageModification(boolean mult, double value) {
        this.mult = mult;
        this.value = value;
    }

    public boolean isMult() {
        return mult;
    }

    public double getValue() {
        return value;
    }
}
