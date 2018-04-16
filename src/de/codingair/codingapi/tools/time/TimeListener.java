package de.codingair.codingapi.tools.time;

public interface TimeListener<E> {
    void onRemove(E item);
    void onTick(E item, int timeLeft);
}
