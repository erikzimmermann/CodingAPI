package de.codingair.codingapi.transfer.packets.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet {
    void write(DataOutputStream out) throws IOException;

    void read(DataInputStream in) throws IOException;
}
