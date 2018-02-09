package de.codingair.codingapi.utils;

public interface Ticker {
    void onTick();
    void onSecond();
    Object getInstance();
}
