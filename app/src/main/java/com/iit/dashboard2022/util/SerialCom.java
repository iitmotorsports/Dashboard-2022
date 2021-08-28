package com.iit.dashboard2022.util;

import androidx.annotation.Nullable;

public abstract class SerialCom {

    public interface ConnectionListener {
        void onSerialConnection(boolean connected);
    }

    public interface ConnectionStateListener {
        void onSerialOpen(boolean open);
    }

    public interface DataListener {
        void newSerialData(byte[] data);
    }

    public interface ErrorListener {
        void newError(Exception exception);
    }

    protected ConnectionStateListener connectionStateListener;
    protected ConnectionListener connectionListener;
    protected DataListener dataListener;
    protected ErrorListener errorListener;

    public void setDataListener(@Nullable DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void setConnectionListener(@Nullable SerialCom.ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void setConnectionStateListener(@Nullable SerialCom.ConnectionStateListener connectionStateListener) {
        this.connectionStateListener = connectionStateListener;
    }

    public void setErrorListener(@Nullable SerialCom.ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public abstract boolean open();

    public abstract void close();

    public abstract boolean isConnected();

    public abstract boolean isOpen();

    public abstract void write(byte[] buffer);
}
