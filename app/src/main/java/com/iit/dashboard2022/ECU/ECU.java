package com.iit.dashboard2022.ECU;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.util.ByteSplit;
import com.iit.dashboard2022.util.LogFileIO;
import com.iit.dashboard2022.util.USBSerial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.util.Date;

public class ECU {
    private final String LOG_TAG = "ECU";

    USBSerial serial;
    ECUKeyMap ecuKeyMap;
    private final LogFileIO logFile;
    InterpretListener interpretListener;
    MODE interpreterMode = MODE.DISABLED;
    boolean fileLogging = true;

    public enum MODE {
        DISABLED,
        ASCII,
        HEX,
        RAW
    }

    public interface InterpretListener {
        void run(String msg);
    }

    public ECU(Activity activity) {
        logFile = new LogFileIO(activity);
        ecuKeyMap = new ECUKeyMap(activity);

        ecuKeyMap.addStatusListener(jsonLoaded -> {
            if (jsonLoaded) {
                ECUMsg.loadMessageKeys();
                logFile.newLog();
            }
        });

        ECUMsg.loadMessages(ecuKeyMap);
        serial = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE, data -> {
            long epoch = System.nanoTime();
            if (interpreterMode != MODE.DISABLED) {
                String msg = processData(epoch, data);
                if (interpretListener != null && msg.length() > 0) {
                    interpretListener.run(msg);
                }
            } else {
                consumeData(epoch, data);
            }
        });
    }

    public void loadJSON() {
        ecuKeyMap.loadJSON();
    }

    public boolean loadJSON(String jsonString) {
        return ecuKeyMap.loadJSON(jsonString);
    }

    public void addStatusListener(@NonNull ECUKeyMap.StatusListener statusListener) {
        ecuKeyMap.addStatusListener(statusListener);
    }

    public void setInterpreterMode(MODE mode) {
        this.interpreterMode = mode;
    }

    public void setLogListener(InterpretListener interpretListener) {
        this.interpretListener = interpretListener;
    }

    public void setOnAttachListener(Runnable onAttachListener) {
        if (serial != null)
            serial.setAttachCallback(onAttachListener);
    }

    public void setOnDetachListener(Runnable onDetachListener) {
        if (serial != null)
            serial.setDetachCallback(onDetachListener);
    }

    public void enableFileLogging(boolean enabled) {
        if (fileLogging != enabled) {
            fileLogging = enabled;
            if (enabled)
                logFile.newLog();
        }
    }

    private void logRawData(long epoch, byte[] msgBlock) {
        if (fileLogging) {
            logFile.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(epoch).array());
            logFile.write(msgBlock);
        }
    }

    /**
     * Gets the ids for a ECU message
     *
     * @param data raw byte data
     * @return the array of id numbers and number
     */
    public static long[] interpretMsg(byte[] data) { // TODO: check that the `get`s are done correctly
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int callerID = buf.getShort() & 0xffff;
        int stringID = buf.getShort() & 0xffff;
        long number = buf.getInt() & 0xffffffffL;
        long msgID = buf.getInt(0);

        return new long[]{callerID, stringID, number, msgID};
    }


    /**
     * Update requested values and run callback
     *
     * @param data_block 8 byte data block
     * @return the message that was updated
     */
    @Nullable
    private ECUMsg updateData(byte[] data_block) {
        long[] IDs = interpretMsg(data_block);
        long msgID = IDs[3];
        return ECUMsg.updateMessages(msgID, IDs[2]);
    }

    /**
     * Consume data, does not interpret anything about it
     * <p>
     * Should be faster than processData, but does not output anything
     *
     * @param raw_data received byte array
     */
    private void consumeData(long epoch, byte[] raw_data) {
        for (int i = 0; i < raw_data.length; i += 8) {
            byte[] data_block = new byte[8];
            try {
                System.arraycopy(raw_data, i, data_block, 0, 8);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.w(LOG_TAG, "Received cutoff array");
                continue;
            }
            updateData(data_block);
            logRawData(epoch, data_block);
        }
    }

    private static String formatMsg(long epoch, String tagString, String msgString, long number) {
        if (tagString == null || msgString == null)
            return "";
        String epochStr = epoch != 0 ? DateFormat.getTimeInstance().format(new Date(epoch)) + ' ' : "";
        return epochStr + tagString + ' ' + msgString + ' ' + number + '\n';
    }

    /**
     * Both Consume and interpret raw data that has been received
     *
     * @param raw_data received byte array
     * @return The interpreted string of the data
     */
    private String processData(long epoch, byte[] raw_data) { // Improve: run this on separate thread
        StringBuilder output = new StringBuilder(32);

        if (interpreterMode == MODE.HEX) {
            for (int i = 0; i < raw_data.length; i += 8) {
                byte[] data_block = new byte[8];
                try {
                    System.arraycopy(raw_data, i, data_block, 0, 8);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.w(LOG_TAG, "Received cutoff array");
                    continue;
                }
                updateData(data_block);
                logRawData(epoch, data_block);
                output.append(ByteSplit.bytesToHex(data_block)).append("\n");
            }
            if (output.length() == 0)
                return "";
            return output.substring(0, output.length() - 1);
        } else if (interpreterMode == MODE.ASCII) {
            for (int i = 0; i < raw_data.length; i += 8) {
                byte[] data_block = new byte[8];
                try {
                    System.arraycopy(raw_data, i, data_block, 0, 8);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }

                logRawData(epoch, data_block);
                ECUMsg msg = updateData(data_block);
                if (msg != null)
                    output.append(formatMsg(epoch, msg.stringTag, msg.stringMsg, msg.value));
            }
            if (output.length() == 0) {
                Log.w(LOG_TAG, "USB serial might be overwhelmed!");
                return output.toString();
            }
            return output.substring(0, output.length() - 1);
        } else { // TODO: process data in ascii format
            return new String(raw_data);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        ecuKeyMap.onActivityResult(requestCode, resultCode, resultData);
    }
}
