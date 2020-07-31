package de.codingair.codingapi.tools.time;


public class Timer {
    private long start = 0;

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public long stop() {
        return System.currentTimeMillis() - this.start;
    }

    public String result() {
        return stop() + "ms";
    }
}
