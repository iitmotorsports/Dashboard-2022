package com.iit.dashboard2022.ecu;

import android.os.SystemClock;
import android.view.Gravity;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.collect.Lists;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.LogFile;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.util.ByteSplit;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.USBSerial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ECU {
    public static final String LOG_TAG = "ECU";

    private final USBSerial usbMethod;
    private static final ByteBuffer logBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    private static final int iBuf_CallerID = 0;
    private static final int iBuf_StringID = 1;
    private static final int iBuf_Value = 2;
    private static final int iBuf_MsgID = 3;
    private final ECUMessageHandler ecuMessageHandler;
    private final ECUJUSB J_USB;
    private final List<Consumer<State>> stateListener = Lists.newArrayList();
    private MODE interpreterMode = MODE.DISABLED;
    private int errorCount = 0;
    private State state = State.INITIALIZING;

    public static ECU instance;

    private static final Logger logger = LoggerFactory.getLogger("ECU");

    public ECU(AppCompatActivity activity) {
        ECU.instance = this;
        J_USB = new ECUJUSB(this);
        ecuMessageHandler = new ECUMessageHandler();

        ecuMessageHandler.getStatistic(Constants.Statistics.State).addMessageListener(val -> {
            State state = State.getStateById(val.intValue());
            if (state == null) {
                //TODO: How the fuck
                return;
            }
            this.state = state;
            stateListener.forEach(consumer -> consumer.accept(state));
        }, ECUStat.UpdateMethod.ON_VALUE_CHANGE);

        // Start Serial
        usbMethod = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE);
        usbMethod.setDataListener(this::receiveData);
        usbMethod.autoConnect(true);
        open();
    }

    private void receiveData(byte[] data) {
        if (J_USB.JUSB_requesting != 0 && J_USB.receive(data)) {
            return;
        }

        if (!ecuMessageHandler.loaded()) {
            if (errorCount == 0) {
                Log.toast("No JSON map Loaded", ToastLevel.WARNING, false, Gravity.START);
            }
            errorCount = ++errorCount % 8;
            return;
        }

        if (interpreterMode != ECU.MODE.DISABLED) {
            processData(data);
        } else {
            consumeData(data);
        }
    }

    public void issueCommand(Command command) {
        usbMethod.write(command.getData());
    }

    public void clear() {
        ecuMessageHandler.clear();
    }

    public void onStateChangeEvent(Consumer<State> consumer) {
        this.stateListener.add(consumer);
    }

    public long requestMsgID(String stringTag, String stringMsg) {
        if (ecuMessageHandler != null) {
            return ecuMessageHandler.requestMsgID(stringTag, stringMsg);
        }
        return -1;
    }

    public void setInterpreterMode(MODE mode) {
        this.interpreterMode = mode;
    }

    /**
     * Log a message's raw data, if possible
     *
     * @param iBuffer The message byte array
     */
    private void logRawData(long[] iBuffer) {
        LogFile activeLogFile = Log.getInstance().getActiveLogFile();
        if(activeLogFile != null) {
            activeLogFile.logBinaryStatistics((int) iBuffer[iBuf_CallerID], (int) iBuffer[iBuf_Value]);
        }
    }

    public void debugUpdate(byte[] data_block) {
        interpretMsg(data_block);
    }

    /**
     * Interprets an ECUMsg and puts it's respective IDs into the given array
     *
     * @param data_block the 8 byte data block
     */
    public long[] interpretMsg(byte[] data_block) { // TODO: check that the `get`s are done correctly
        long[] iBuffer = new long[4];
        ByteBuffer buf = ByteBuffer.wrap(data_block).order(ByteOrder.LITTLE_ENDIAN);
        iBuffer[iBuf_CallerID] = buf.getShort() & 0xffff;
        iBuffer[iBuf_StringID] = buf.getShort() & 0xffff;
        iBuffer[iBuf_Value] = buf.getInt() & 0xffffffffL;
        iBuffer[iBuf_MsgID] = buf.getInt(0);

        // TODO: Need to update here?
        ecuMessageHandler.updateStatistic((int) iBuffer[iBuf_CallerID], (int) iBuffer[iBuf_StringID], iBuffer[iBuf_Value]);
        return iBuffer;
    }

    /**
     * Consume data, does not interpret anything about it
     * <p>
     * Should be faster than processData, but does not output anything
     *
     * @param raw_data received byte array
     */
    private void consumeData(byte[] raw_data) {
        for (int i = 0; i < raw_data.length; i += 8) {
            byte[] data_block = new byte[8];
            try {
                System.arraycopy(raw_data, i, data_block, 0, 8);
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Received cutoff array.");
                continue;
            }
            long[] iBuffer = interpretMsg(data_block);
            logRawData(iBuffer);
        }
    }

    /**
     * Both Consume and interpret raw data that has been received
     *
     * @param raw_data received byte array
     * @return The interpreted string of the data
     */
    private void processData(byte[] raw_data) { // Improve: run this on separate thread

        if (interpreterMode == MODE.HEX) {
            for (int i = 0; i < raw_data.length; i += 8) {
                byte[] data_block = new byte[8];
                try {
                    System.arraycopy(raw_data, i, data_block, 0, 8);
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.error("Received cutoff array.");
                    continue;
                }
                long[] iBuffer = interpretMsg(data_block);
                logRawData(iBuffer);
                logger.debug(ByteSplit.bytesToHex(data_block));
            }
        } else if (interpreterMode == MODE.ASCII) {
            for (int i = 0; i < raw_data.length; i += 8) {
                byte[] data_block = new byte[8];
                try {
                    System.arraycopy(raw_data, i, data_block, 0, 8);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
                long[] iBuffer = interpretMsg(data_block);
                logRawData(iBuffer);
                String message = ecuMessageHandler.getStr((int) iBuffer[iBuf_StringID]);
                int temp = (int) iBuffer[iBuf_CallerID];
                if((temp < 256 || temp > 4096) && message != null) {
                    String comp = message.toLowerCase(Locale.ROOT);
                    long val = iBuffer[iBuf_Value];
                    if(comp.contains("[error]")) {
                        logger.error(message.replace("[ERROR]", "").trim() + val);
                    } else if(comp.contains("[fatal]")) {
                        logger.error(message.replace("[FATAL]", "").trim() + val);
                    } else if(comp.contains("[warn]")) {
                        logger.warn(message.replace("[WARN]", "").trim() + val);
                    } else if(comp.contains("[debug]")) {
                        logger.debug(message.replace("[DEBUG]", "").trim() + val);
                    } else {
                        logger.info(message.replace("[INFO]", "").replace("[ LOG ]", "").trim() + val);
                    }
                }
            }
        } else {
            consumeData(raw_data); // attempt to process data
        }
    }

    public ECUMessageHandler getMessageHandler() {
        return ecuMessageHandler;
    }

    public ECUJUSB getUsb() {
        return J_USB;
    }

    public void write(byte[] data) {
        usbMethod.write(data);
    }

    public void setConnectionListener(Consumer<Integer> statusListener) {
        usbMethod.setStatusListener(statusListener);
    }

    public boolean open() {
        return usbMethod.open();
    }

    public void close() {
        usbMethod.close();
    }

    public boolean isOpen() {
        return usbMethod.isOpen();
    }

    public boolean isAttached() {
        return usbMethod.isAttached();
    }

    /**
     * Gets current state of the vehicle
     *
     * @return the current state of the vehicle
     */
    public State getState() {
        return state;
    }

    public enum MODE {
        DISABLED,
        ASCII,
        HEX,
        RAW
    }

    public enum Command {
        CHARGE(123),
        SEND_CAN_BUS_MESSAGE(111),
        CLEAR_FAULT(45),
        TOGGLE_CAN_BUS_SNIFF(127),
        TOGGLE_MIRROR_MODE(90),
        ENTER_MIRROR_SET(-1),
        SEND_ECHO(84),
        TOGGLE_REVERSE(25),
        PRINT_LOOKUP(101),
        SET_SERIAL_VAR(61);

        private final byte[] data;

        Command(int id) {
            this.data = new byte[]{ Integer.valueOf(id).byteValue() };
        }

        public byte[] getData() {
            return data;
        }
    }

    public enum State { // Use actual name, brackets are added on when matching to actual state name
        INITIALIZING("Teensy Initialize"),
        PRE_CHARGE("PreCharge State"),
        IDLE("Idle State"),
        CHARGING("Charging State"),
        BUTTON("Button State"),
        DRIVING("Driving Mode State"),
        FAULT("Fault State");

        private final String name;
        private int id = -1;

        State(String title) {
            this.name = title;
        }

        public void setTagId(int id) {
            this.id = id;
        }

        public int getTagId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static State getStateById(int id) {
            for (State state : State.values()) {
                if (state.getTagId() == id) {
                    return state;
                }
            }
            return null;
        }

        public static State getStateByName(String name) {
            for (State state : State.values()) {
                if (state.getName().equalsIgnoreCase(name)) {
                    return state;
                }
            }
            return null;
        }
    }
}
