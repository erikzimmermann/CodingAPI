package de.codingair.codingapi.transfer.packets.bungee;

import de.codingair.codingapi.transfer.packets.utils.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InitialPacket implements Packet {
    private String version, serverName;

    public InitialPacket() {
    }

    public InitialPacket(String version, String serverName) {
        this.version = version;
        this.serverName = serverName;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.version);
        out.writeUTF(this.serverName);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.version = in.readUTF();
        this.serverName = in.readUTF();
    }

    public String getVersion() {
        return version;
    }

    public String getServerName() {
        return serverName;
    }
}
