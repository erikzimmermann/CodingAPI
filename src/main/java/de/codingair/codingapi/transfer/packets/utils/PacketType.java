package de.codingair.codingapi.transfer.packets.utils;

import de.codingair.codingapi.transfer.packets.bungee.InitialPacket;
import de.codingair.codingapi.transfer.packets.general.BooleanPacket;
import de.codingair.codingapi.transfer.packets.general.IntegerPacket;
import de.codingair.codingapi.transfer.packets.general.LongPacket;
import de.codingair.codingapi.transfer.packets.spigot.RequestInitialPacket;

public enum PacketType {
    InitialPacket(InitialPacket.class),
    RequestInitialPacket(RequestInitialPacket.class),
    BooleanPacket(BooleanPacket.class),
    IntegerPacket(IntegerPacket.class),
    LongPacket(LongPacket.class),
    AnswerPacket(AnswerPacket.class),
    ;

    public static final PacketType[] VALUES = values();
    private final Class<?> packet;

    PacketType(Class<?> packet) {
        this.packet = packet;
    }

    public Class<?> getPacket() {
        return packet;
    }
}
