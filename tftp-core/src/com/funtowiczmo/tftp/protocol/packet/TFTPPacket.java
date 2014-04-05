package com.funtowiczmo.tftp.protocol.packet;

/**
 * Created by Morgan on 03/04/2014.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Holder for basic information about TFTPPacket. All derivate packet have to subclass TFTPPacket.
 */
public abstract class TFTPPacket implements SerializablePacket{

    protected short opcode;

    public TFTPPacket(short opcode){
        this.opcode = opcode;
    }

    /**
     * Return the opcode flag of this packet
     * @return TFTP Opcode
     */
    public int getOpcode() {
        return opcode;
    }

    @Override
    public byte[] serializePacket() throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(bytes);

        fillRawPacket(out);

        final byte[] raw = bytes.toByteArray();

        out.close();

        return raw;
    }

    /**
     * Abstract method to define what a packet has to write into the output
     * @param out Output buffer
     * @throws IOException
     */
    protected abstract void fillRawPacket(DataOutputStream out) throws IOException;

    @Override
    public String toString() {
        return "TFTPPacket{" +
                "opcode=" + opcode +
                '}';
    }
}
