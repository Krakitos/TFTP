package com.funtowiczmo.tftp.protocol.packet;

/**
 * Created by Morgan on 03/04/2014.
 */
public interface AcknowledgeablePacket {
    /**
     * Get the block number to acknowledge
     * @return Block Number >= 0
     */
    public short getBlockNumber();
}
