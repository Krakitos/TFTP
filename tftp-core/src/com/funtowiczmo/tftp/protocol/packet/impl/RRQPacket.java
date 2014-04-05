package com.funtowiczmo.tftp.protocol.packet.impl;

import com.funtowiczmo.tftp.protocol.TFTPOpCodesEnum;
import com.funtowiczmo.tftp.protocol.enums.TFTPIOEnum;
import com.funtowiczmo.tftp.protocol.errors.TFTPFormatException;
import com.funtowiczmo.tftp.protocol.packet.TFTPPacket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Morgan on 03/04/2014.
 */
public class RRQPacket extends TFTPPacket {

    private File localStorageFile;
    private String remoteFileName;
    private TFTPIOEnum mode;

    public RRQPacket(){
        super(TFTPOpCodesEnum.RRQ);
    }

    public RRQPacket(File local, String remoteFileName, TFTPIOEnum mode) {
        super(TFTPOpCodesEnum.RRQ);

        this.localStorageFile = local;
        this.remoteFileName = remoteFileName;
        this.mode = mode;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public File getLocalStorageFile() {
        return localStorageFile;
    }

    public TFTPIOEnum getMode() {
        return mode;
    }

    @Override
    public TFTPPacket deserializePacket(byte[] data) throws IOException, TFTPFormatException {
        //Cannot be sent by server
        return this;
    }

    @Override
    protected void fillRawPacket(DataOutputStream out) throws IOException {
        out.writeShort(opcode);
        out.writeBytes(remoteFileName);
        out.writeByte(0);
        out.writeBytes(mode.getValue());
        out.writeByte(0);
    }

    @Override
    public String toString() {
        return "RRQPacket{" +
                "remoteFile='" + localStorageFile.getAbsolutePath() + '\'' +
                ", mode=" + mode.name() +
                '}';
    }
}
