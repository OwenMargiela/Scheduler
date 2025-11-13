package src.core;

import java.util.concurrent.ThreadLocalRandom;

public enum TaskLoad {
    SHORT(1, 15),
    MODERATE(16, 30),
    LONG(31, 100);

    private final int minBurst;
    private final int maxBurst;

    TaskLoad(int minBurst, int maxBurst) {
        this.minBurst = minBurst;
        this.maxBurst = maxBurst;
    }

    public int randomBurstTime() {
        return ThreadLocalRandom.current().nextInt(minBurst, maxBurst + 1);
    }
}