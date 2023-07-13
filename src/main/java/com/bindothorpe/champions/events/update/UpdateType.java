package com.bindothorpe.champions.events.update;

public enum UpdateType {

    TICK(49L), RAPID(99L), FAST(249L), HALF_SECOND(499L), SECOND(1000L), TWO_SECOND(2000L), FIVE_SECOND(5000L), MINUTE(60000L), TWO_MINUTES(120000L), FIVE_MINUTES(3000000L);

    private final long time;

    UpdateType(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
