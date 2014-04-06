package com.funtowiczmo.tftp.client.observers;

import com.funtowiczmo.tftp.client.TFTPClient;
import com.funtowiczmo.tftp.protocol.errors.TFTPException;

import java.io.File;

/**
 * Created by Morgan on 03/04/2014.
 */
public interface TFTPObserver {

    public void onFileSendingStarted(File sourceFile);
    public void onFileSendingProgress(float percent);
    public void onFileSendingEnded(TFTPClient client, File sourceFile);

    public void onFileReceptionStarted(String remoteFileName);
    public void onFileReceptionEnded(TFTPClient client, File holder);

    public void onExceptionOccured(TFTPClient client, Exception t);
    public void onProtocolError(TFTPClient client, int errno, String errorMsg);
}
