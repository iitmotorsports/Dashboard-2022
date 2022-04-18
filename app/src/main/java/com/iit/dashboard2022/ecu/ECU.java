package com.iit.dashboard2022.ecu;

import android.util.Pair;
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * A class that handles the communication and representation of the vehicle's engine control unit.
 *
 * @author Isaias Rivera
 * @author Noah Husby
 */
public class ECU {
    private static final Logger logger = LoggerFactory.getLogger("ECU");

    private final ECUMessageHandler ecuMessageHandler;
    private final USBSerial usbMethod;
    private final ECUJUSB J_USB;
    private final List<Consumer<State>> stateListener = Collections.synchronizedList(Lists.newArrayList());
    private Mode interpreterMode = Mode.DISABLED;
    private int errorCount = 0;

    private final BlockingQueue<Pair<Long, byte[]>> payloadQueue = new LinkedBlockingQueue<>();


    public ECU(AppCompatActivity activity) {
        J_USB = new ECUJUSB(this);
        ECUMessageHandler.MapHandler.ECU.set(J_USB);
        ecuMessageHandler = new ECUMessageHandler();

        // Handles state management
        ecuMessageHandler.getStatistic(Constants.Statistics.State).addMessageListener(stat -> {
            State state = State.getStateById(stat.getAsInt());
            if (state == null) {
                return;
            }
            stateListener.forEach(consumer -> consumer.accept(state));
        }, ECUStat.UpdateMethod.ON_VALUE_CHANGE);

        // Thread for handling message queue
        Thread ecuThread = new Thread(() -> {
            while (true) {
                try {
                    Pair<Long, byte[]> data = payloadQueue.take();
                    for (int i = 0; i < data.second.length; i += 8) {
                        byte[] data_block = new byte[8];
                        try {
                            System.arraycopy(data.second, i, data_block, 0, 8);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.error("Received cutoff array.");
                            continue;
                        }
                        ECUPayload payload = new ECUPayload(data.first, data_block);
                        handlePayload(payload);
                    }
                } catch (InterruptedException e) {
                    Log.getLogger().warn("ECU Thread Interrupted", e);
                }
            }
        });
        ecuThread.setDaemon(true);
        ecuThread.setName("ECU-Thread");
        ecuThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(ecuThread::interrupt));

        // Start Serial
        usbMethod = new USBSerial(activity, 115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_2, UsbSerialPort.PARITY_NONE);
        usbMethod.setDataListener(this::receiveData);
        usbMethod.autoConnect(true);
        open();
    }

    /**
     * Adds raw payload to queue.
     *
     * @param data Raw message data from ECU.
     */
    public void postPayload(byte[] data) {
        payloadQueue.add(new Pair<>(System.currentTimeMillis(), data));
    }

    /**
     * Handles incoming raw message from ECU.
     *
     * @param data Raw message from ECU.
     */
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
        postPayload(data);
    }

    /**
     * Sends a {@link Command} to the ECU
     *
     * @param command The {@link Command} to send
     */
    public void issueCommand(Command command) {
        usbMethod.write(command.getData());
    }

    /**
     * Event fired each time the {@link State} changes.
     *
     * @param consumer {@link Consumer<State>}
     */
    public void onStateChangeEvent(Consumer<State> consumer) {
        this.stateListener.add(consumer);
    }

    /**
     * Sets the interpreter mode of incoming messages.
     * Valid options include: DISABLED, ASCII, HEX, RAW.
     *
     * @param mode {@link Mode} The mode to be set.
     */
    public void setInterpreterMode(Mode mode) {
        this.interpreterMode = mode;
    }

    /**
     * Log a message's raw data, if possible
     *
     * @param payload The payload from the ECU.
     */
    private void logRawData(ECUPayload payload) {
        LogFile activeLogFile = Log.getInstance().getActiveLogFile();
        if (activeLogFile != null) {
            activeLogFile.logBinaryStatistics((int) payload.getCallerId(), (int) payload.getValue());
        }
    }

    /**
     * Handles the payload based upon the interpreter mode.
     * The raw data is logged to the binary file regardless of mode.
     *
     * @param payload {@link ECUPayload}
     */
    private void handlePayload(ECUPayload payload) {
        if (interpreterMode == Mode.HEX) {
            logger.debug(ByteSplit.bytesToHex(payload.getRawData()));
        } else if (interpreterMode == Mode.ASCII) {
            String message = ecuMessageHandler.getStr((int) payload.getStringId());
            int temp = (int) payload.getCallerId();
            if ((temp < 256 || temp > 4096) && message != null) {
                String comp = message.toLowerCase(Locale.ROOT);
                String val = " " + payload.getValue();
                if (comp.contains("[error]")) {
                    logger.error(message.replace("[ERROR]", "").trim() + val);
                } else if (comp.contains("[fatal]")) {
                    logger.error(message.replace("[FATAL]", "").trim() + val);
                } else if (comp.contains("[warn]")) {
                    logger.warn(message.replace("[WARN]", "").trim() + val);
                } else if (comp.contains("[debug]")) {
                    logger.debug(message.replace("[DEBUG]", "").trim() + val);
                } else {
                    logger.info(message.replace("[INFO]", "").replace("[ LOG ]", "").trim() + val);
                }
            }
        }
        ecuMessageHandler.updateStatistic((int) payload.getCallerId(), (int) payload.getStringId(), payload.getValue());
        logRawData(payload);
    }

    /**
     * Gets the message handler for the ECU.
     *
     * @return {@link ECUMessageHandler}
     */
    public ECUMessageHandler getMessageHandler() {
        return ecuMessageHandler;
    }

    /**
     * Writes a raw payload to the ECU.
     * Caution: Only use this if you know what you're doing.
     *
     * @param data The raw data to be written.
     */
    public void write(byte[] data) {
        usbMethod.write(data);
    }

    /**
     * Event fired each time the ECU connects / disconnects.
     *
     * @param statusListener {@link Consumer<Integer>}
     */
    public void setConnectionListener(Consumer<Integer> statusListener) {
        usbMethod.setStatusListener(statusListener);
    }

    /**
     * Opens the connection to the USB serial device
     *
     * @return True if successfully opened, false if not
     */
    public boolean open() {
        return usbMethod.open();
    }

    /**
     * Attempts to close the connection to the USB serial device
     */
    public void close() {
        usbMethod.close();
    }

    /**
     * Gets whether the connection to the USB device is active
     *
     * @return True if active, false otherwise
     */
    public boolean isOpen() {
        return usbMethod.isOpen();
    }

    /**
     * Gets whether the USB device is attached
     *
     * @return True if attached, false if not
     */
    public boolean isAttached() {
        return usbMethod.isAttached();
    }

    public enum Mode {
        /**
         * No interpretation of data.
         */
        DISABLED,

        /**
         * Data is encoded to ascii log messages.
         */
        ASCII,

        /**
         * Data is encoded as hex values.
         */
        HEX,

        /**
         * Data is encoded in it's original form.
         */
        RAW
    }

    /**
     * An enumeration of ECU commands.
     */
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

        /**
         * Gets the raw data representation of the command.
         *
         * @return Representation of command as a byte array.
         */
        public byte[] getData() {
            return data;
        }
    }

    /**
     * An enumeration of vehicle states.
     */
    public enum State {
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

        /**
         * Sets the numerical ID of the state.
         *
         * @param id ID of state as integer.
         */
        public void setTagId(int id) {
            this.id = id;
        }

        /**
         * Gets the numerical ID of the state.
         *
         * @return ID of state as integer.
         */
        public int getTagId() {
            return id;
        }

        /**
         * Gets the pretty name of the state.
         *
         * @return Pretty name of state.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the state by it's numerical ID.
         *
         * @param id ID of state.
         * @return {@link State} if exists, null otherwise.
         */
        public static State getStateById(int id) {
            for (State state : State.values()) {
                if (state.getTagId() == id) {
                    return state;
                }
            }
            return null;
        }

        /**
         * Gets the state by it's name.
         *
         * @param name Name of state.
         * @return {@link State} if exists, null otherwise.
         */
        public static State getStateByName(String name) {
            name = name.replaceAll("\\[", "").replaceAll("]", "");
            for (State state : State.values()) {
                if (state.getName().equalsIgnoreCase(name)) {
                    return state;
                }
            }
            return null;
        }
    }
}
