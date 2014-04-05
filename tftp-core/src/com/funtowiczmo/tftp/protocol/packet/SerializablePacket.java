package com.funtowiczmo.tftp.protocol.packet;

import com.funtowiczmo.tftp.protocol.errors.TFTPFormatException;

import java.io.IOException;

/**
 * Created by Morgan on 03/04/2014.
 */
public interface SerializablePacket {
    /**
     * Serialize a packet by returning the raw binary representation of the packet
     * @return Byte[] holding data which will be sent over the network
     * @throws IOException
     */
    public byte[] serializePacket() throws IOException;

    /**
     * Deserialize a packet
     * @param data Data from the network
     * @return TFTPPacket instance holding data readed into the raw binary DatagramPacket
     * @throws TFTPFormatException If raw data is malformed
     * @throws IOException
     */
    public TFTPPacket deserializePacket(byte[] data) throws TFTPFormatException, IOException;
}
