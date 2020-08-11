package de.codingair.codingapi.transfer.packets.general;

import de.codingair.codingapi.transfer.packets.utils.AnswerPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerPacket extends AnswerPacket<Integer> {

    public IntegerPacket() {
    }

    public IntegerPacket(int b) {
        super(b);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(getValue());
        super.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        setValue(in.readInt());
        super.read(in);
    }
}
