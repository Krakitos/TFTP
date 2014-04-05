package com.funtowiczmo.tftp.client.observers;

import com.funtowiczmo.tftp.client.TFTPClient;
import com.funtowiczmo.tftp.protocol.errors.TFTPException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Morgan on 03/04/2014.
 */
public abstract class TFTPObservable {
    private List<TFTPObserver> observers;

    public TFTPObservable(){
        observers = new LinkedList<TFTPObserver>();
    }

    public void addObserver(TFTPObserver observer){
        observers.add(observer);
    }

    public void removeObserver(TFTPObserver observer){
        observers.remove(observer);
    }

    public void removeObservers(){
        observers.clear();
    }

    protected void fireFileReceptionStarted(String remoteFileName){
        for(TFTPObserver o : observers){
           o.onFileReceptionStarted(remoteFileName);
        }
    }

    protected void fireFileReceptionEnded(final TFTPClient client,final File holder){
        for(TFTPObserver o : observers){
            o.onFileReceptionEnded(client, holder);
        }
    }

    protected void fireFileSendingStarted(File sourceFile){
        for(TFTPObserver o : observers){
            o.onFileSendingStarted(sourceFile);
        }
    }

    protected void fireFileSendingProgress(float percent){
        for(TFTPObserver o : observers){
            o.onFileSendingProgress(percent);
        }
    }

    protected void fireFileSendingEnded(final TFTPClient client, final File sourceFile){
        for(TFTPObserver o : observers){
            o.onFileSendingEnded(client, sourceFile);
        }
    }

    protected void fireExceptionOccurred(final TFTPClient client, final TFTPException t){
        for(TFTPObserver o : observers){
            o.onExceptionOccured(client, t);
        }
    }

    protected void fireProtocolErrorOccurred(final TFTPClient client, int errno, String errorMsg){
        for(TFTPObserver o : observers){
            o.onProtocolError(client, errno, errorMsg);
        }
    }
}
