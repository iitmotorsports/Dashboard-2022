package com.iit.dashboard2022.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class USBSerial implements SerialInputOutputManager.Listener {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UsbSerialPort.STOPBITS_1, UsbSerialPort.STOPBITS_1_5, UsbSerialPort.STOPBITS_2})
    @interface StopBits {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UsbSerialPort.DATABITS_5, UsbSerialPort.DATABITS_6, UsbSerialPort.DATABITS_7, UsbSerialPort.DATABITS_8})
    @interface DataBits {
    }

    private final Context context;
    private final UsbManager usbManager;
    private final PendingIntent deviceIntent;
    private final UsbDataListener usbDataListener;
    private final BroadcastReceiver broadcastReceiver;
    private final int baudRate, dataBits, stopBits, parity;

    private boolean active;
    private UsbSerialPort port;
    private UsbActiveListener usbActiveListener;
    private UsbAttachListener usbAttachListener;
    private ErrorListener errorListener;

    public interface UsbActiveListener {
        void run(boolean active);
    }

    public interface UsbAttachListener {
        void run(boolean attached);
    }

    public interface UsbDataListener {
        void newUsbData(byte[] data);
    }

    public interface ErrorListener {
        void newError(Exception exception);
    }

    public USBSerial(Context context, int baudRate, @DataBits int dataBits, @StopBits int stopBits, @UsbSerialPort.Parity int parity, @NonNull UsbDataListener usbDataListener) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        deviceIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbManager.EXTRA_PERMISSION_GRANTED), PendingIntent.FLAG_IMMUTABLE);
        this.usbDataListener = usbDataListener;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        if (open()) {
                            if (usbAttachListener != null)
                                usbAttachListener.run(true);
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED: // TODO: ensure the thing detached was the thing that last connected
                        if (usbAttachListener != null) {
                            usbAttachListener.run(false);
                        }
                        break;
                }
            }
        };

        this.context = context;

        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(broadcastReceiver, filter);
    }

    private boolean openNewConnection() {
        if (isOpen())
            return true;
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            return false;
        }
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
        if (connection == null) {
            usbManager.requestPermission(driver.getDevice(), deviceIntent);
            return false;
        }
        port = driver.getPorts().get(0); // Assume device has one port
        try {
            port.open(connection);
            port.setParameters(baudRate, dataBits, stopBits, parity);
            new SerialInputOutputManager(port, this).start();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean open() {
        boolean opened = openNewConnection();
        if (usbActiveListener != null)
            usbActiveListener.run(opened);
        active = opened;
        return opened;
    }

    public void close() {
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setUsbAttachListener(UsbAttachListener usbAttachListener) {
        this.usbAttachListener = usbAttachListener;
    }

    public void setUsbActiveListener(UsbActiveListener usbActiveListener) {
        this.usbActiveListener = usbActiveListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public boolean isOpen() {
        return port != null && active;
    }

    public void write(byte[] buffer) {
        try {
            port.write(buffer, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        context.unregisterReceiver(broadcastReceiver);
        super.finalize();
    }


    @Override
    public void onNewData(byte[] data) {
        usbDataListener.newUsbData(data);
    }

    @Override
    public void onRunError(Exception e) {
        if (errorListener != null)
            errorListener.newError(e);
        if (isOpen()) {
            active = false;
            if (usbActiveListener != null)
                usbActiveListener.run(false);
        }
    }

}
