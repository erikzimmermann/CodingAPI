package de.codingair.codingapi.transfer.core;

import de.codingair.codingapi.transfer.packets.utils.Packet;

public interface PacketListener<P> {
    void onReceive(Packet packet, P player);

    boolean onSend(Packet packet);
}
