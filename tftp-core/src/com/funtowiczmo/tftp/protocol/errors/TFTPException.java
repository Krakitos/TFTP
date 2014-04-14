package com.funtowiczmo.tftp.protocol.errors;

/**
 * Created by Morgan on 03/04/2014.
 */
public class TFTPException extends Exception {
    public TFTPException() {
    }

    public TFTPException(String message) {
        super(message);
    }

    public TFTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public TFTPException(Throwable cause) {
        super(cause);
    }
}
