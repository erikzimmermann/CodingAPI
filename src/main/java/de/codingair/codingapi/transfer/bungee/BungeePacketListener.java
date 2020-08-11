package de.codingair.codingapi.transfer.bungee;

import de.codingair.codingapi.transfer.packets.utils.Packet;
import de.codingair.codingapi.transfer.utils.PacketListener;
import net.md_5.bungee.api.config.ServerInfo;

public interface BungeePacketListener extends PacketListener {
    @Override
    default void onReceive(Packet packet, Object server) {
        onReceive(packet, (ServerInfo) server);
    }

    void onReceive(Packet packet, ServerInfo server);
}
