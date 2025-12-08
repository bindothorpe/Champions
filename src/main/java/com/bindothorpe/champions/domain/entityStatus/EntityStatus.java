package com.bindothorpe.champions.domain.entityStatus;

import java.util.Objects;

/**
 * Represents a status effect that can be applied to an entity.
 *
 * <p>Status effects modify entity attributes such as damage, knockback, or movement speed.
 * Each status has a type, value, optional duration, and flags indicating whether it's
 * a multiplier or absolute value.</p>
 *
 * <p>Status effects are immutable once created, though the {@link #multiplyValue(int)}
 * method can create a new modified copy.</p>
 *
 * @author bindothorpe
 * @see EntityStatusType
 * @see EntityStatusManager
 */
public class EntityStatus {

    private final EntityStatusType type;
    private double value;
    private final double duration;
    private final boolean isMultiplier;
    private final boolean isAbsolute;
    private final Object source;

    /**
     * Creates a new EntityStatus with the specified parameters.
     *
     * @param type The type of status effect (e.g., DAMAGE_DONE, MOVEMENT_SPEED)
     * @param value The numerical value of the status effect
     * @param duration The duration in seconds that this status lasts, or -1 for permanent
     * @param isMultiplier True if this value should be applied multiplicatively, false for additive
     * @param isAbsolute True if this status overrides all other statuses of the same type, false to stack
     * @param source The source object that applied this status (used for identification and removal)
     */
    public EntityStatus(EntityStatusType type, double value, double duration, boolean isMultiplier,boolean isAbsolute, Object source) {
        this.type = type;
        this.value = value;
        this.duration = duration;
        this.isMultiplier = isMultiplier;
        this.isAbsolute = isAbsolute;
        this.source = source;
    }

    /**
     * Gets the type of this status effect.
     *
     * @return The EntityStatusType of this status
     */
    public EntityStatusType getType() {
        return type;
    }

    /**
     * Gets the numerical value of this status effect.
     *
     * <p>For additive statuses, this value is added to the base value.
     * For multiplier statuses, this value is added to the multiplier (starting from 1.0).</p>
     *
     * @return The value of this status effect
     */
    public double getValue() {
        return value;
    }

    /**
     * Gets the duration of this status effect in seconds.
     *
     * @return The duration in seconds, or -1 if the status is permanent
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Checks if this status is a multiplier.
     *
     * <p>Multiplier statuses are applied after additive modifications in the formula:
     * (baseValue + additiveModifications) * multiplierModifications</p>
     *
     * @return True if this is a multiplier status, false if additive
     */
    public boolean isMultiplier() {
        return isMultiplier;
    }

    /**
     * Checks if this status is absolute.
     *
     * <p>Absolute statuses override all other statuses of the same type and calculation mode
     * (additive or multiplicative). Only one absolute status can be active at a time.</p>
     *
     * @return True if this status is absolute and overrides others, false if it stacks
     */
    public boolean isAbsolute() {
        return isAbsolute;
    }

    /**
     * Creates a new EntityStatus with the value multiplied by the specified multiplier.
     *
     * <p>This method returns a new instance rather than modifying the current one,
     * preserving the immutability of status effects.</p>
     *
     * @param multiplier The multiplier to apply to the current value
     * @return A new EntityStatus with the multiplied value and all other properties unchanged
     */
    public EntityStatus multiplyValue(int multiplier) {
        return new EntityStatus(type, value * multiplier, duration, isMultiplier, isAbsolute, source);
    }

    /**
     * Gets the source object that applied this status effect.
     *
     * <p>The source is used to identify and remove specific status effects, allowing
     * the same entity to have multiple statuses of the same type from different sources.</p>
     *
     * @return The source object that applied this status
     */
    public Object getSource() {
        return source;
    }

    /**
     * Compares this EntityStatus to another object for equality.
     *
     * <p>Two EntityStatus objects are considered equal if all their properties
     * (type, value, duration, isMultiplier, isAbsolute, and source) are equal.</p>
     *
     * @param o The object to compare with
     * @return True if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityStatus that = (EntityStatus) o;
        return Double.compare(that.value, value) == 0 && Double.compare(that.duration, duration) == 0 && isMultiplier == that.isMultiplier && isAbsolute == that.isAbsolute && type == that.type && Objects.equals(source, that.source);
    }

    /**
     * Generates a hash code for this EntityStatus.
     *
     * <p>The hash code is based on all properties of the status effect.</p>
     *
     * @return The hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, value, duration, isMultiplier, isAbsolute, source);
    }
}