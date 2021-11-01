package com.iit.dashboard2022.ecu;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.iit.dashboard2022.util.ByteSplit;
import com.iit.dashboard2022.util.LogFileIO;
import com.iit.dashboard2022.util.SerialCom;
import com.iit.dashboard2022.util.USBSerial;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ECU {
    public static final String LOG_TAG = "ECU";
    private static final ByteBuffer logBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

    private final ECUCommunication com;
    private final ECUMsgHandler ecuMsgHandler;
    private final ECUKeyMap ecuKeyMap;
    private final ECULogger ecuLogger;

    private Runnable jsonLoadListener;
    private InterpretListener interpretListener;
    private ErrorListener errorListener;
    private MODE interpreterMode = MODE.DISABLED;
    private final ECUJUSB JUSB;
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
        JUSB = new ECUJUSB(this);
        ecuLogger = new ECULogger(activity);
        ecuKeyMap = new ECUKeyMap(activity);
        ecuMsgHandler = new ECUMsgHandler(ecuKeyMap);

        ecuKeyMap.addStatusListener((jsonLoaded, rawJson) -> {
            if (jsonLoaded) {
                ecuMsgHandler.loadMessageKeys();
                if (rawJson != null)
                    ecuLogger.newLog(rawJson);
                if (jsonLoadListener != null)
                    jsonLoadListener.run();
            }
        });

        com = new ECUCommunication(activity, data -> {
            if (JUSB.JUSB_requesting != 0 && JUSB.receive(data))
                return;

            long epoch = SystemClock.elapsedRealtime();

            if (interpreterMode != ECU.MODE.DISABLED) {
                String msg = processData(epoch, data);
                if (interpretListener != null && msg.length() > 0) {
                    interpretListener.newMessage(msg);
                }
            } else {
                consumeData(epoch, data);
            }
        });
    }

    public void requestJSONFromUSBSerial() {// Request ZLib compressed JSON map if we are connected directly
        if (com.getCurrentMethod() instanceof USBSerial)
            JUSB.request();
    }

    public void issueCommand(@ECUCommands.ECUCommand int Command) {
        com.write(ECUCommands.COMMANDS[Command]);
    }

    public void clear() {
        ecuKeyMap.clear();
    }

    public boolean open() {
        return com.open();
    }

    public void close() {
        com.close();
    }

    public boolean isOpen() {
        return com.isOpen();
    }

    public boolean isAttached() {
        return com.isConnected();
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

    public void setJSONLoadListener(Runnable listener) {
        this.jsonLoadListener = listener;
    }

    public void addStatusListener(@NonNull ECUKeyMap.StatusListener statusListener) {
        ecuKeyMap.addStatusListener(statusListener);
    }

    public long requestMsgID(String stringTag, String stringMsg) {
        if (ecuKeyMap != null)
            return ecuKeyMap.requestMsgID(stringTag, stringMsg);
        return -1;
    }

    public void setInterpreterMode(MODE mode) {
        this.interpreterMode = mode;
    }

    public void setLogListener(InterpretListener interpretListener) {
        this.interpretListener = interpretListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        com.setErrorListener(exception -> errorListener.newError("Serial", "Thread Stopped: " + exception.getMessage()));
    }

    public void setConnectionListener(SerialCom.ConnectionListener ConnectionListener) {
        com.setConnectionListener(ConnectionListener);
    }

    public void setConnectionStateListener(SerialCom.ConnectionStateListener connectionStateListener) {
        com.setConnectionStateListener(connectionStateListener);
    }

    public ECUMsgHandler getEcuMsgHandler() {
        return ecuMsgHandler;
    }

    public ECUCommunication getEcuCommunicator() {
        return com;
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

    private final long[] iBuffer = new long[4];
    private static final int iBuf_CallerID = 0;
    private static final int iBuf_StringID = 1;
    private static final int iBuf_Value = 2;
    private static final int iBuf_MsgID = 3;

    /**
     * Interprets an ECUMsg and puts it's respective IDs into the given array
     *
     * @param iBuffer    the length 4 long array for the msg IDs
     * @param data_block the 8 byte data block
     */
    public static void interpretMsg(long[] iBuffer, byte[] data_block) { // TODO: check that the `get`s are done correctly
        ByteBuffer buf = ByteBuffer.wrap(data_block).order(ByteOrder.LITTLE_ENDIAN);
        iBuffer[iBuf_CallerID] = buf.getShort() & 0xffff;
        iBuffer[iBuf_StringID] = buf.getShort() & 0xffff;
        iBuffer[iBuf_Value] = buf.getInt() & 0xffffffffL;
        iBuffer[iBuf_MsgID] = buf.getInt(0);
    }

    public void debugUpdate(byte[] data_block) {
        updateData(data_block);
    }

    /**
     * Only update needed values and run callback
     *
     * @param data_block 8 byte data block
     * @return ECUMsg that was updated, null in none
     */
    private ECUMsg updateData(byte[] data_block) {
        interpretMsg(iBuffer, data_block);
        String fault = ecuMsgHandler.checkFaults(iBuffer[iBuf_StringID]);
        if (fault != null && errorListener != null) {
            errorListener.newError("CAN Fault", fault);
            return null;
        }
        return ecuMsgHandler.updateMessages(iBuffer[iBuf_MsgID], iBuffer[iBuf_Value]);
    }

    private static final Date d = new Date();

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
        d.setTime(epoch);
        String epochStr = epoch != 0 ? DateFormat.getTimeInstance().format(d) + ' ' : "";
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
        ECUMsg msg = updateData(data_block);
        if (msg == null) {
            return formatMsg(epoch, ecuKeyMap.getTag((int) iBuffer[iBuf_CallerID]), ecuKeyMap.getStr((int) iBuffer[iBuf_StringID]), iBuffer[iBuf_Value]);
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
        } else {
            consumeData(epoch, raw_data); // attempt to process data
            return new String(raw_data);
        }
    }

}
