package de.codingair.codingapi.transfer.packets.utils;

import de.codingair.codingapi.tools.Callback;

public class RequestPacket<E> extends AssignedPacket {
    private final Callback<E> callback;

    public RequestPacket() {
        this(null);
    }

    public RequestPacket(Callback<E> callback) {
        this.callback = callback;
    }

    public Callback<E> getCallback() {
        return callback;
    }
}
