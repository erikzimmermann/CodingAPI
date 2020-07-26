package de.codingair.codingapi.time;


public class Timer {
    private double lastStoppedTime = 0;
    private long start = 0;

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public void stop() {
        this.lastStoppedTime = ((int) (System.currentTimeMillis() - this.start)) / 1000;
    }

    public double getLastStoppedTime() {
        return lastStoppedTime;
    }
}
