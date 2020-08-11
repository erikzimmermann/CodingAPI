package de.codingair.codingapi.transfer.packets.utils;

public interface PacketHandler {
    void handle(Packet packet, String... extra);
}
