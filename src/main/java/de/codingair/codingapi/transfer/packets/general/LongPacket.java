package de.codingair.codingapi.transfer.packets.general;

import de.codingair.codingapi.transfer.packets.utils.AnswerPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongPacket extends AnswerPacket<Long> {

    public LongPacket() {
    }

    public LongPacket(long b) {
        super(b);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeLong(getValue());
        super.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        setValue(in.readLong());
        super.read(in);
    }
}
