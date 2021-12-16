package com.iit.dashboard2022.ecu;

import android.app.Activity;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.util.NearbySerial;
import com.iit.dashboard2022.util.SerialCom;
import com.iit.dashboard2022.util.USBSerial;

public class ECUCommunication {
    private USBSerial USBMethod;
    private NearbySerial nearbyMethod;
    private SerialCom.DataListener dataListener;

    protected void startSerial(Activity activity, SerialCom.DataListener receiveData) {
        this.dataListener = receiveData;
        USBMethod = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE);
        USBMethod.setDataListener(receiveData);
        USBMethod.autoConnect(true);

        nearbyMethod = new NearbySerial(activity);
        nearbyMethod.setDataListener(receiveData);
    }

    public void setErrorListener(SerialCom.ErrorListener errorListener) {
        USBMethod.setErrorListener(errorListener);
        nearbyMethod.setErrorListener(errorListener);
    }

    public void setConnectionListener(SerialCom.StatusListener StatusListener) {
        USBMethod.setStatusListener(StatusListener);
        nearbyMethod.setStatusListener(flags -> {
            if (!USBMethod.isAttached()) StatusListener.newStatus(flags);
        });
    }

    public void enableNearbyAPI(boolean isChecked) {
        if (isChecked) {
            nearbyMethod.open();
            USBMethod.setDataListener(data -> {
                dataListener.newData(data);
                nearbyMethod.write(data);
            });
        } else {
            nearbyMethod.close();
            USBMethod.setDataListener(dataListener);
        }
    }

    public void write(byte[] data) {
        USBMethod.write(data);
    }

    public boolean open() {
        return USBMethod.open();
    }

    public void close() {
        USBMethod.close();
    }

    public boolean isOpen() {
        return USBMethod.isOpen();
    }

    public boolean isAttached() {
        return USBMethod.isAttached();
    }
}
