package src.core;

import java.util.concurrent.atomic.AtomicInteger;

public class Ticker {
    private final AtomicInteger time;

    public Ticker() {
        this.time = new AtomicInteger(0);
    }

    public int getTime() {
        return time.get();
    }

    public void tick() {
        time.incrementAndGet();
    }

    public void advance(int dt) {
        time.addAndGet(dt);
    }
}
