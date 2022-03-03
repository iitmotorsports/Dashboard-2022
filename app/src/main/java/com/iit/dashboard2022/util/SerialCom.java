package com.iit.dashboard2022.util;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Consumer;

public abstract class SerialCom {

    public static final int Attached = 1 << 0; // Physical connection
    public static final int Detached = 1 << 1; // Physical disconnection
    public static final int Opened = 1 << 2; // Digital connection
    public static final int Closed = 1 << 3; // Digital disconnection
    protected int status = 0;
    protected Consumer<Integer> statusListener;
    protected Consumer<byte[]> dataListener;
    protected Consumer<Exception> errorListener;

    protected void newConnData(byte[] buffer) {
        if (dataListener != null) {
            dataListener.accept(buffer);
        }
    }

    protected void newConnError(Exception exception) {
        if (errorListener != null) {
            errorListener.accept(exception);
        }
    }

    protected void setConnStatus(int flags) {
        status = flags;
        if (statusListener != null) {
            statusListener.accept(flags);
        }
    }

    public boolean checkStatus(int flags) {
        return (status & flags) == flags;
    }

    public void setDataListener(@Nullable Consumer<byte[]> data) {
        this.dataListener = data;
    }

    public void setStatusListener(@Nullable Consumer<Integer> flags) {
        this.statusListener = flags;
    }

    public void setErrorListener(@Nullable Consumer<Exception> exception) {
        this.errorListener = exception;
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
    @IntDef({ Attached, Detached, Opened, Closed })
    @interface Status {
    }
}
