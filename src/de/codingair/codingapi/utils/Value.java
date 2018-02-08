package de.codingair.codingapi.utils;

public class Value<E> {
    private E value;
    private boolean changed = false;

    public Value(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }
}
