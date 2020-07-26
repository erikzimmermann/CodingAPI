package de.codingair.codingapi.utils;

public class ImprovedDouble {
    private final Number n;

    public ImprovedDouble(double n) {
        this.n = n;
    }

    public Number get() {
        if(n.intValue() == n.doubleValue()) return n.intValue();
        else return n.doubleValue();
    }

    @Override
    public String toString() {
        return get() + "";
    }
}
