package com.iit.dashboard2022.ecu;

import android.app.Activity;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.util.NearbySerial;
import com.iit.dashboard2022.util.SerialCom;
import com.iit.dashboard2022.util.USBSerial;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ECUCommunication extends SerialCom {
    private final USBSerial USBMethod;
    private final NearbySerial nearbyMethod;
    private SerialCom currentMethod;
    private int current = -1;

    ECUCommunication(@NonNull Activity activity, @NonNull DataListener dataListener) {

        USBMethod = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE);
        nearbyMethod = new NearbySerial(activity);

        setDataListener(data -> {
            dataListener.newSerialData(data);
            if (current != NEARBY)
                nearbyMethod.write(data);
        });

        USBMethod.setDataListener(this.dataListener);
        nearbyMethod.setDataListener(this.dataListener);

        currentMethod = USBMethod;
        changeMethod(USB);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USB, NEARBY})
    @interface SerialUpdateMethod {
    }

    public static final int USB = 0;
    public static final int NEARBY = 1;

    public void changeMethod(@SerialUpdateMethod int updateMethod) {
        switch (updateMethod) {
            case USB:
                switchCurrentMethod(USBMethod);
                current = updateMethod;
                break;
            case NEARBY:
                switchCurrentMethod(nearbyMethod);
                current = updateMethod;
                break;
        }
    }

    private void switchCurrentMethod(@NonNull SerialCom newMethod) {
        currentMethod.close();
        currentMethod = newMethod;
        currentMethod.setConnectionListener(connectionListener);
        currentMethod.setConnectionStateListener(connectionStateListener);
        currentMethod.setErrorListener(errorListener);
    }

    @Override
    public void setConnectionListener(SerialCom.ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        currentMethod.setConnectionListener(connectionListener);
    }

    @Override
    public void setConnectionStateListener(SerialCom.ConnectionStateListener connectionStateListener) {
        this.connectionStateListener = connectionStateListener;
        currentMethod.setConnectionStateListener(connectionStateListener);
    }

    @Override
    public void setErrorListener(SerialCom.ErrorListener errorListener) {
        this.errorListener = errorListener;
        currentMethod.setErrorListener(errorListener);
    }

    @Override
    public boolean open() {
        return currentMethod.open();
    }

    @Override
    public void close() {
        currentMethod.close();
    }

    @Override
    public boolean isConnected() {
        return currentMethod.isConnected();
    }

    @Override
    public boolean isOpen() {
        return currentMethod.isOpen();
    }

    @Override
    public void write(byte[] buffer) {
        currentMethod.write(buffer);
    }
}
