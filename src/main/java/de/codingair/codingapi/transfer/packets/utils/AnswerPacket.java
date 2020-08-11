package de.codingair.codingapi.transfer.packets.utils;

public class AnswerPacket<E> extends AssignedPacket {
    private E value;

    public AnswerPacket() {
    }

    public AnswerPacket(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }
}
