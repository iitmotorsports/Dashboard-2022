package com.iit.dashboard2022.ecu;

import androidx.appcompat.app.AppCompatActivity;
import com.google.common.collect.Lists;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.LogFile;
import com.iit.dashboard2022.util.USBSerial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
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

    private final USBSerial usbMethod;
    private final List<Consumer<State>> stateListener = Collections.synchronizedList(Lists.newArrayList());

    private final BlockingQueue<byte[]> payloadQueue = new LinkedBlockingQueue<>();



    public ECU(AppCompatActivity activity) {

        // Handles state management
        Metric.STATE.addMessageListener(stat -> {
            State state = State.getStateById(stat.getValue());
            if (state == null) {
                return;
            }
            stateListener.forEach(consumer -> consumer.accept(state));
        });

        // Thread for handling message queue
        Thread ecuThread = new Thread(() -> {
            while (true) {
                try {
                    ByteBuffer buf = ByteBuffer.wrap(payloadQueue.take())
                            .order(ByteOrder.LITTLE_ENDIAN);
                    handlePayload(buf.getInt(0), buf.getInt(1));
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
        usbMethod.setDataListener(payloadQueue::add);
        usbMethod.autoConnect(true);
        open();
    }

    /**
     * Handles the payload based upon the interpreter mode.
     * The raw data is logged to the binary file regardless of mode.
     *
     * @param id ID of the metric.
     * @param value Value of the metric.
     */
    private void handlePayload(int id, int value) {
        Metric metric = Metric.getMetricById(id);
        if (metric == null) {
            logger.warn("Invalid metric w/ ID: {}", id);
            return;
        }
        metric.update(value);
        LogFile activeLogFile = Log.getInstance().getActiveLogFile();
        if (activeLogFile != null) {
            activeLogFile.logBinaryStatistics(metric);
        }
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

    /**
     * An enumeration of ECU commands.
     */
    public enum Command {
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
    }
}
