package com.iit.dashboard2022.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import androidx.annotation.IntDef;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class USBSerial extends SerialCom implements SerialInputOutputManager.Listener {

    private final Context context;
    private final UsbManager usbManager;
    private final PendingIntent deviceIntent;
    private final IntentFilter broadcastFilter;
    private final BroadcastReceiver broadcastReceiver;
    private final int baudRate, dataBits, stopBits, parity;
    private UsbSerialPort port;
    private boolean registered = false;

    public USBSerial(Context context, int baudRate, @DataBits int dataBits, @StopBits int stopBits, @UsbSerialPort.Parity int parity) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        deviceIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbManager.EXTRA_PERMISSION_GRANTED), PendingIntent.FLAG_IMMUTABLE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        if (open())
                            setConnStatus(Attached | Opened);
                        else
                            setConnStatus(Attached | Closed);
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED: // TODO: ensure the thing detached was the thing that last connected
                        setConnStatus(Detached | Closed);
                        break;
                }
            }
        };

        this.context = context;

        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;

        broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        broadcastFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    }

    public void autoConnect(boolean enabled) {
        if (enabled) {
            context.registerReceiver(broadcastReceiver, broadcastFilter);
            registered = true;
        } else if (registered) {
            context.unregisterReceiver(broadcastReceiver);
            registered = false;
        }
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
        if (isOpen())
            return true;
        boolean opened = openNewConnection();
        if (opened)
            setConnStatus(SerialCom.Attached | SerialCom.Opened);
        return opened;
    }

    @Override
    public void close() {
        if (!isOpen())
            return;
        if (port != null) {
            try {
                port.close();
                port = null;
                setConnStatus((checkStatus(SerialCom.Attached) ? SerialCom.Attached : SerialCom.Detached) | SerialCom.Closed);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        newConnData(data);
    }

    @Override
    public void onRunError(Exception e) {
        newConnError(e);
        close();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UsbSerialPort.STOPBITS_1, UsbSerialPort.STOPBITS_1_5, UsbSerialPort.STOPBITS_2})
    @interface StopBits {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UsbSerialPort.DATABITS_5, UsbSerialPort.DATABITS_6, UsbSerialPort.DATABITS_7, UsbSerialPort.DATABITS_8})
    @interface DataBits {
    }

}
