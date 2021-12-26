package com.iit.dashboard2022.util;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class SerialCom {

    public static final int Attached = 1 << 0; // Physical connection
    public static final int Detached = 1 << 1; // Physical disconnection
    public static final int Opened = 1 << 2; // Digital connection
    public static final int Closed = 1 << 3; // Digital disconnection
    protected int status = 0;
    protected StatusListener statusListener;
    protected DataListener dataListener;
    protected ErrorListener errorListener;

    protected void newConnData(byte[] buffer) {
        if (dataListener != null)
            dataListener.newData(buffer);
    }

    protected void newConnError(Exception exception) {
        if (errorListener != null)
            errorListener.newError(exception);
    }

    protected void setConnStatus(int flags) {
        status = flags;
        if (statusListener != null)
            statusListener.newStatus(flags);
    }

    public boolean checkStatus(int flags) {
        return (status & flags) == flags;
    }

    public void setDataListener(@Nullable DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void setStatusListener(@Nullable StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void setErrorListener(@Nullable ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public abstract boolean open();

    public abstract void close();

    public boolean isOpen() {
        return (status & Opened) == Opened;
    }

    public boolean isAttached() {
        return (status & Attached) == Attached;
    }

    public abstract void write(byte[] buffer);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Attached, Detached, Opened, Closed})
    @interface Status {
    }

    public interface StatusListener {
        void newStatus(int flags);
    }

    public interface DataListener {
        void newData(byte[] data);
    }

    public interface ErrorListener {
        void newError(Exception exception);
    }
}
