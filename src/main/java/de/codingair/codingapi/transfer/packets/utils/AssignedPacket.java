package de.codingair.codingapi.transfer.packets.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public abstract class AssignedPacket implements Packet {
    protected UUID uniqueId;

    public AssignedPacket() {
        this.uniqueId = UUID.randomUUID();
    }

    public AssignedPacket(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeLong(this.uniqueId.getMostSignificantBits());
        out.writeLong(this.uniqueId.getLeastSignificantBits());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.uniqueId = new UUID(in.readLong(), in.readLong());
    }

    public void applyAsAnswer(AssignedPacket packet) {
        packet.uniqueId = this.uniqueId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void checkUUID(Set<UUID> taken) {
        while(taken.contains(this.uniqueId)) {
            this.uniqueId = UUID.randomUUID();
        }
    }
}
