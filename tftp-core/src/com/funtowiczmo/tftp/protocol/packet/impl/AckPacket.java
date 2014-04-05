package com.funtowiczmo.tftp.protocol.packet.impl;

import com.funtowiczmo.tftp.protocol.TFTPOpCodesEnum;
import com.funtowiczmo.tftp.protocol.errors.TFTPFormatException;
import com.funtowiczmo.tftp.protocol.packet.TFTPPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Morgan on 03/04/2014.
 */
public class AckPacket extends TFTPPacket {

    private short blockID;

    public AckPacket() {
        super(TFTPOpCodesEnum.ACK);
    }

    public AckPacket(short blockID){
        super(TFTPOpCodesEnum.ACK);
        this.blockID = blockID;
    }

    @Override
    protected void fillRawPacket(DataOutputStream out) throws IOException {
        out.writeShort(opcode);
        out.writeShort(blockID);
    }

    @Override
    public TFTPPacket deserializePacket(byte[] data) throws TFTPFormatException, IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

        if(in.readShort() != TFTPOpCodesEnum.ACK) throw new TFTPFormatException("Wrong opcode");
        blockID = in.readShort();

        in.close();

        return this;
    }

    public short getBlockID() {
        return blockID;
    }

    @Override
    public String toString() {
        return "AckPacket{" +
                "blockID=" + blockID +
                '}';
    }
}
