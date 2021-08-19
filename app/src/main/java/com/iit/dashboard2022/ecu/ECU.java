package com.iit.dashboard2022.ecu;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.util.ByteSplit;
import com.iit.dashboard2022.util.LogFileIO;
import com.iit.dashboard2022.util.USBSerial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ECU {
    public static final String LOG_TAG = "ECU";
    private static final ByteBuffer logBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

    private final USBSerial serial;
    private final ECUMsgHandler ecuMsgHandler;
    private final ECUKeyMap ecuKeyMap;
    private final ECULogger ecuLogger;

    InterpretListener interpretListener;
    ErrorListener errorListener;
    MODE interpreterMode = MODE.DISABLED;
    boolean fileLogging = true;

    public enum MODE {
        DISABLED,
        ASCII,
        HEX,
        RAW
    }

    public interface InterpretListener {
        void newMessage(String msg);
    }

    public interface ErrorListener {
        void newError(String tag, String msg);
    }

    public ECU(AppCompatActivity activity) {
        ecuLogger = new ECULogger(activity);
        ecuKeyMap = new ECUKeyMap(activity);
        ecuMsgHandler = new ECUMsgHandler(ecuKeyMap);

        ecuKeyMap.addStatusListener((jsonLoaded, rawJson) -> {
            if (jsonLoaded) {
                ecuMsgHandler.loadMessageKeys();
                if (rawJson != null) {
                    ecuLogger.newLog(rawJson);
                }
            }
        });

        serial = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE, data -> {
            long epoch = SystemClock.elapsedRealtime();
            if (interpreterMode != MODE.DISABLED) {
                String msg = processData(epoch, data);
                if (interpretListener != null && msg.length() > 0) {
                    interpretListener.newMessage(msg);
                }
            } else {
                consumeData(epoch, data);
            }
        });
    }

    public void clear() {
        ecuKeyMap.clear();
    }

    public boolean open() {
        return serial.open();
    }

    public void close() {
        serial.close();
    }

    public boolean isOpen() {
        return serial.isOpen();
    }

    public boolean isAttached() {
        return serial.isAttached();
    }

    public void requestJSONFile() {
        ecuKeyMap.requestJSONFile();
    }

    public boolean loadJSONFromSystem() {
        return ecuKeyMap.loadJSONFromSystem();
    }

    public boolean loadJSONString(String jsonString) {
        return ecuKeyMap.loadJSONString(jsonString);
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

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        serial.setErrorListener(exception -> errorListener.newError("Serial", "Thread Stopped: " + exception.getMessage()));
    }

    public void setUsbAttachListener(USBSerial.UsbAttachListener UsbAttachListener) {
        serial.setUsbAttachListener(UsbAttachListener);
    }

    public void setUsbActiveListener(USBSerial.UsbActiveListener usbActiveListener) {
        serial.setUsbActiveListener(usbActiveListener);
    }

    public ECUMsgHandler getEcuMsgHandler() {
        return ecuMsgHandler;
    }

    public List<LogFileIO.LogFile> getLocalLogs() {
        return ecuLogger.getLocalLogs();
    }

    /**
     * Set whether logging to a local file is enabled
     *
     * @param enabled Whether logging is enabled
     */
    public void enableFileLogging(boolean enabled) {
        if (fileLogging != enabled) {
            fileLogging = enabled;
        }
    }

    /**
     * Log a message's raw data, if possible
     *
     * @param epoch    Epoch when message was sent
     * @param msgBlock The message byte array
     */
    private void logRawData(long epoch, byte[] msgBlock) {
        if (fileLogging) {
            logBuffer.clear();
            ecuLogger.write(logBuffer.putLong(epoch).array());
            ecuLogger.write(msgBlock);
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
     * Only update needed values and run callback
     *
     * @param data_block 8 byte data block
     */
    private void updateData(byte[] data_block) {
        long[] IDs = interpretMsg(data_block);
        long msgID = IDs[3];
        ecuMsgHandler.updateMessages(msgID, IDs[2]);
    }

    /**
     * Generate a formatted string to be passed to interpretListener
     *
     * @param epoch     Epoch when message was sent
     * @param tagString The message's tag
     * @param msgString The message's string
     * @param number    The message's value
     * @return Formatted message string
     */
    static String formatMsg(long epoch, String tagString, String msgString, long number) {
        if (tagString == null || msgString == null)
            return "";
        String epochStr = epoch != 0 ? DateFormat.getTimeInstance().format(new Date(epoch)) + ' ' : "";
        return epochStr + tagString + ' ' + msgString + ' ' + number + '\n';
    }

    /**
     * Update requested values, run callback, and generate a formatted string from message
     *
     * @param epoch      Epoch when message was sent
     * @param data_block 8 byte data block
     * @return the formatted message that was received
     */
    private String updateFormattedData(long epoch, byte[] data_block) {
        long[] IDs = interpretMsg(data_block);
        long msgID = IDs[3];
        ECUMsg msg = ecuMsgHandler.updateMessages(msgID, IDs[2]);
        if (msg == null) {
            return formatMsg(epoch, ecuKeyMap.getTag((int) IDs[0]), ecuKeyMap.getStr((int) IDs[1]), IDs[2]);
        } else {
            return formatMsg(epoch, msg.stringTag, msg.stringMsg, msg.value);
        }
    }

    /**
     * Consume data, does not interpret anything about it
     * <p>
     * Should be faster than processData, but does not output anything
     *
     * @param epoch    Epoch when message was sent
     * @param raw_data received byte array
     */
    private void consumeData(long epoch, byte[] raw_data) {
        for (int i = 0; i < raw_data.length; i += 8) {
            byte[] data_block = new byte[8];
            try {
                System.arraycopy(raw_data, i, data_block, 0, 8);
            } catch (ArrayIndexOutOfBoundsException e) {
                if (errorListener != null)
                    errorListener.newError(LOG_TAG, "Received cutoff array");
                continue;
            }
            updateData(data_block);
            logRawData(epoch, data_block);
        }
    }

    /**
     * Both Consume and interpret raw data that has been received
     *
     * @param epoch    Epoch when message was sent
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
                    if (errorListener != null)
                        errorListener.newError(LOG_TAG, "Received cutoff array");
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
                output.append(updateFormattedData(epoch, data_block));
            }
            if (output.length() == 0) {
                if (errorListener != null)
                    errorListener.newError(LOG_TAG, "USB serial might be overwhelmed!");
                return output.toString();
            }
            return output.substring(0, output.length() - 1);
        } else { // TODO: process data in ascii format
            return new String(raw_data);
        }
    }

}
