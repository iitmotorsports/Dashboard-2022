package com.iit.dashboard2022;

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
import java.util.Objects;

public class USBSerial implements SerialInputOutputManager.Listener {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UsbSerialPort.STOPBITS_1, UsbSerialPort.STOPBITS_1_5, UsbSerialPort.STOPBITS_2})
    @interface StopBits {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UsbSerialPort.DATABITS_5, UsbSerialPort.DATABITS_6, UsbSerialPort.DATABITS_7, UsbSerialPort.DATABITS_8})
    @interface DataBits {
    }

    private final UsbManager usbManager;
    private final PendingIntent deviceIntent;
    private final UsbReadCallback readCallback;
    private final int baudRate, dataBits, stopBits, parity;

    private UsbSerialPort port;
    private Runnable detachCallback;
    private ErrorCallback errorCallback;

    public interface UsbReadCallback {
        void onNewData(byte[] data);
    }

    public interface ErrorCallback {
        void run(Exception exception);
    }

    public USBSerial(Context context, int baudRate, @DataBits int dataBits, @StopBits int stopBits, @UsbSerialPort.Parity int parity, @NonNull UsbReadCallback readCallback) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        deviceIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbManager.EXTRA_PERMISSION_GRANTED), PendingIntent.FLAG_IMMUTABLE);
        this.readCallback = readCallback;

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())) {
                    case UsbManager.EXTRA_PERMISSION_GRANTED:
                        break;
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        open();
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        if (detachCallback != null)
                            detachCallback.run();
                        break;
                }
            }
        };

        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.EXTRA_PERMISSION_GRANTED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(broadcastReceiver, filter);
    }

    public boolean open() {
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

    public void close() {
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDetachCallback(Runnable detachCallback) {
        this.detachCallback = detachCallback;
    }

    public void setErrorCallback(ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    public boolean isOpen() {
        return port != null && port.isOpen();
    }

    public void write(byte[] buffer) {
        try {
            port.write(buffer, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewData(byte[] data) {
        readCallback.onNewData(data);
    }

    @Override
    public void onRunError(Exception e) {
        if (errorCallback != null)
            errorCallback.run(e);
    }

}
