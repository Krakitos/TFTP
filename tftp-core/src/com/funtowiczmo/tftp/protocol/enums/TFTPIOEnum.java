package com.funtowiczmo.tftp.protocol.enums;

/**
 * Created by Morgan on 03/04/2014.
 */
public enum TFTPIOEnum {
    NETASCII("netascii"),
    OCTET("octet"),
    MAIL("mail");

    private final String mode;

    TFTPIOEnum(String type) {
        mode = type;
    }

    public String getValue() {
        return mode;
    }
}
