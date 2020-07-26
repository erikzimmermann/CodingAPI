package de.codingair.codingapi.server.blocks.utils;

public enum Axis {
    X(0, (byte) 0),
    Y(1, (byte) 0),
    Z(2, (byte) 2);

    private final int id;
    private final byte byteId;

    Axis(int id, byte byteId) {
        this.id = id;
        this.byteId = byteId;
    }

    public byte getByteId() {
        return byteId;
    }

    public int getId() {
        return id;
    }
}
