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

public class USBSerial extends SerialCom implements SerialInputOutputManager.Listener {

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
    private final BroadcastReceiver broadcastReceiver;
    private final int baudRate, dataBits, stopBits, parity;

    private boolean active, attached;
    private UsbSerialPort port;

    public USBSerial(Context context, int baudRate, @DataBits int dataBits, @StopBits int stopBits, @UsbSerialPort.Parity int parity) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        deviceIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbManager.EXTRA_PERMISSION_GRANTED), PendingIntent.FLAG_IMMUTABLE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        if (open()) {
                            if (connectionListener != null)
                                connectionListener.onSerialConnection(true);
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED: // TODO: ensure the thing detached was the thing that last connected
                        if (connectionListener != null) {
                            connectionListener.onSerialConnection(false);
                            attached = false;
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

    @Override
    public boolean open() {
        boolean opened = openNewConnection();
        if (connectionStateListener != null)
            connectionStateListener.onSerialOpen(opened);
        active = opened;
        if (opened && !attached) {
            attached = true;
            if (connectionListener != null)
                connectionListener.onSerialConnection(true);
        }
        return opened;
    }

    @Override
    public void close() {
        if (port != null) {
            try {
                port.close();
                port = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isConnected() {
        return attached;
    }

    @Override
    public boolean isOpen() {
        return active;
    }

    @Override
    public void write(byte[] buffer) {
        try {
            if (port != null && isOpen())
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
        dataListener.newSerialData(data);
    }

    @Override
    public void onRunError(Exception e) {
        if (errorListener != null)
            errorListener.newError(e);
        if (isOpen()) {
            active = false;
            if (connectionStateListener != null)
                connectionStateListener.onSerialOpen(false);
        }
    }

}
