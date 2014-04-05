package com.funtowiczmo.tftp.protocol.packet.impl;

import com.funtowiczmo.tftp.protocol.TFTPOpCodesEnum;
import com.funtowiczmo.tftp.protocol.errors.TFTPFormatException;
import com.funtowiczmo.tftp.protocol.packet.AcknowledgeablePacket;
import com.funtowiczmo.tftp.protocol.packet.TFTPPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Morgan on 03/04/2014.
 */
public class DataPacket extends TFTPPacket implements AcknowledgeablePacket {

    private short blockNumber;
    private byte[] data;
    private int offset;
    private int length;

    public DataPacket() {
        super(TFTPOpCodesEnum.DATA);
    }

    public DataPacket(short blockNumber, byte[] data, int offset, int length){
        super(TFTPOpCodesEnum.DATA);
        this.blockNumber = blockNumber;
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public short getBlockNumber() {
        return blockNumber;
    }


    @Override
    /**
     * !!!!! WARNING : Length must be defined before deserialize packet !!!!!
     */
    public TFTPPacket deserializePacket(byte[] data) throws TFTPFormatException, IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

        if(in.readShort() != TFTPOpCodesEnum.DATA) throw new TFTPFormatException("Wrong opcode");

        blockNumber = in.readShort();
        this.data = new byte[data.length - 4]; //4 first bytes (opcode / blockNumber / data)

        System.arraycopy(data, 4, this.data, 0, length);

        in.close();

        return this;
    }

    @Override
    protected void fillRawPacket(DataOutputStream out) throws IOException {
        out.writeShort(opcode);
        out.writeShort(blockNumber);
        out.write(data, offset, length);
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "blockNumber=" + blockNumber +
                ", length=" + length +
                '}';
    }
}
