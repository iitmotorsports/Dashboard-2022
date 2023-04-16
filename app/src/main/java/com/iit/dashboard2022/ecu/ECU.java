package com.iit.dashboard2022.ecu;

import androidx.appcompat.app.AppCompatActivity;
import com.google.common.collect.Lists;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.LogFile;
import com.iit.dashboard2022.util.USBSerial;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class ECU {

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
                    while (buf.hasRemaining()) {
                        handlePayload(buf.getInt(), buf.getInt());
                    }
                } catch (InterruptedException e) {
                    log.warn("ECU Thread Interrupted", e);
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
     * @param id    ID of the metric.
     * @param value Value of the metric.
     */
    private void handlePayload(int id, int value) {
        Metric metric = Metric.getMetricById(id);
        if (metric == null) {
            log.warn("Invalid metric w/ ID: {}", id);
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
     * An enumeration of ECU commands.
     */
    public enum Command {
        TOGGLE_REVERSE(25),
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

}
