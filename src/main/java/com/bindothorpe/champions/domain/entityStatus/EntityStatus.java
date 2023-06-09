package com.bindothorpe.champions.domain.entityStatus;

import java.util.Objects;

public class EntityStatus {

    private final EntityStatusType type;
    private double value;
    private final double duration;
    private final boolean isMultiplier;
    private final boolean isAbsolute;
    private final Object source;

    public EntityStatus(EntityStatusType type, double value, double duration, boolean isMultiplier,boolean isAbsolute, Object source) {
        this.type = type;
        this.value = value;
        this.duration = duration;
        this.isMultiplier = isMultiplier;
        this.isAbsolute = isAbsolute;
        this.source = source;
    }

    public EntityStatusType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getDuration() {
        return duration;
    }

    public boolean isMultiplier() {
        return isMultiplier;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public EntityStatus multiplyValue(int multiplier) {
        return new EntityStatus(type, value * multiplier, duration, isMultiplier, isAbsolute, source);
    }

    public Object getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityStatus that = (EntityStatus) o;
        return Double.compare(that.value, value) == 0 && Double.compare(that.duration, duration) == 0 && isMultiplier == that.isMultiplier && isAbsolute == that.isAbsolute && type == that.type && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, duration, isMultiplier, isAbsolute, source);
    }
}
