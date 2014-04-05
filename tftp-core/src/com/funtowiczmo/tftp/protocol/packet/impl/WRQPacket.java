package com.funtowiczmo.tftp.protocol.packet.impl;

import com.funtowiczmo.tftp.protocol.TFTPOpCodesEnum;
import com.funtowiczmo.tftp.protocol.enums.TFTPIOEnum;
import com.funtowiczmo.tftp.protocol.packet.TFTPPacket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by Morgan on 03/04/2014.
 */
public class WRQPacket extends TFTPPacket{

    private File localStorage;
    private String storageName;
    private TFTPIOEnum mode;

    public WRQPacket() {
        super(TFTPOpCodesEnum.WRQ);
    }

    public WRQPacket(File localStorage, String storageName, TFTPIOEnum mode) {
        super(TFTPOpCodesEnum.WRQ);
        this.localStorage = localStorage;
        this.storageName = storageName;
        this.mode = mode;
    }

    public File getLocalStorage(){return localStorage; }

    public String getStorageName() {
        return storageName;
    }

    public TFTPIOEnum getMode() {
        return mode;
    }

    @Override
    public TFTPPacket deserializePacket(byte[] data) {
        //Cannot be sent by server
        return this;
    }

    @Override
    protected void fillRawPacket(DataOutputStream out) throws IOException {
        out.writeShort(opcode);
        out.writeBytes(storageName);
        out.writeByte(0);
        out.writeBytes(mode.getValue());
        out.writeByte(0);
    }

    @Override
    public String toString() {
        return "WRQPacket{" +
                "localStorage=" + localStorage +
                ", storageName='" + storageName + '\'' +
                ", mode=" + mode +
                '}';
    }
}
