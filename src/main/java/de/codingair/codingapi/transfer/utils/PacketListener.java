package de.codingair.codingapi.transfer.utils;

import de.codingair.codingapi.transfer.packets.utils.Packet;

public interface PacketListener {
    void onReceive(Packet packet, Object server);

    boolean onSend(Packet packet);
}
