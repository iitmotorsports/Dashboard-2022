package com.iit.dashboard2022.ecu;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.util.NearbySerial;
import com.iit.dashboard2022.util.SerialCom;
import com.iit.dashboard2022.util.Toaster;
import com.iit.dashboard2022.util.USBSerial;

public class ECUCommunication extends SerialCom {
    private final NearbySerial nearbyMethod;
    private final USBSerial USBMethod;

    private DataListener savedListener;
    private SerialCom currentMethod;
    private boolean nearbyRelayEnabled = false;

    ECUCommunication(@NonNull Activity activity, @NonNull DataListener dataListener) {
        USBMethod = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE);
        nearbyMethod = new NearbySerial(activity);

        savedListener = data -> {
            dataListener.newSerialData(data);
            nearbyMethod.write(data);
        };

        setDataListener(dataListener);
        currentMethod = USBMethod;
        switchCurrentMethod(USBMethod);
    }

    private void enableNearbyRelay(boolean enabled) {
        if (enabled == nearbyRelayEnabled)
            return;
        DataListener temp = this.dataListener;
        setDataListener(savedListener);
        currentMethod.setDataListener(savedListener);
        savedListener = temp;
        nearbyRelayEnabled = !nearbyRelayEnabled;
        if (!enabled)
            nearbyMethod.close();
    }

    public void enableNearbyAPI(boolean enabled) {
        if (currentMethod.equals(USBMethod) && currentMethod.isOpen()) {
            if (enabled) {
                Toaster.showToast("Sending Nearby Serial", Toaster.INFO);
                enableNearbyRelay(true);
                nearbyMethod.open();
            } else {
                enableNearbyRelay(false);
            }
            return;
        }
        enableNearbyRelay(false);
        if (enabled) {
            Toaster.showToast("Receiving Nearby Serial", Toaster.WARNING);
            USBMethod.autoConnect(false);
            switchCurrentMethod(nearbyMethod);
            nearbyMethod.open();
        } else {
            Toaster.showToast("Using USB Serial", Toaster.WARNING);
            switchCurrentMethod(USBMethod);
            USBMethod.autoConnect(true);
            USBMethod.open();
        }
    }

    private void switchCurrentMethod(@NonNull SerialCom newMethod) {
        currentMethod.close();
        currentMethod.setDataListener(null);
        currentMethod.setConnectionListener(null);
        currentMethod.setConnectionStateListener(null);
        currentMethod.setErrorListener(null);

        currentMethod = newMethod;

        currentMethod.setDataListener(this.dataListener);
        currentMethod.setConnectionListener(this.connectionListener);
        currentMethod.setConnectionStateListener(this.connectionStateListener);
        currentMethod.setErrorListener(this.errorListener);
    }

    @Override
    public void setConnectionListener(SerialCom.ConnectionListener connectionListener) {
        super.setConnectionListener(connectionListener);
        currentMethod.setConnectionListener(connectionListener);
    }

    @Override
    public void setConnectionStateListener(SerialCom.ConnectionStateListener connectionStateListener) {
        super.setConnectionStateListener(connectionStateListener);
        currentMethod.setConnectionStateListener(connectionStateListener);
    }

    @Override
    public void setErrorListener(SerialCom.ErrorListener errorListener) {
        super.setErrorListener(errorListener);
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
