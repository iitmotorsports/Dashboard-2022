package com.iit.dashboard2022.ecu;

import com.iit.dashboard2022.util.ByteSplit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ECUStat {

    private final String identifier;
    private int id = -1;
    private String prettyName;
    public final Map<Consumer<ECUStat>, UpdateMethod> messageListeners = new ConcurrentHashMap<>();
    private long value = 0;

    public ECUStat(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier for the statistic
     *
     * @return Identifier of statistic
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the pretty name of the statistic
     *
     * @return the pretty name
     */
    public String getPrettyName() {
        return prettyName;
    }

    /**
     * Gets the id of the statistic
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Initializes the statistic with proper information
     *
     * @param id         Mapping id of statistic
     * @param prettyName Pretty name of statistic
     */
    public void initialize(int id, String prettyName) {
        this.id = id;
        this.prettyName = prettyName;
    }

    /**
     * Update the message from the ECU
     *
     * @param val The value of the field
     * @implNote This should only be called internal to {@link ECU}
     */
    public void update(long val) {
        long prevValue = this.value;
        this.value = val;

        for (Map.Entry<Consumer<ECUStat>, UpdateMethod> entry : messageListeners.entrySet()) {
            Consumer<ECUStat> consumer = entry.getKey();
            switch (entry.getValue()) {
                case ON_VALUE_CHANGE:
                    if (prevValue != value) {
                        consumer.accept(this);
                    }
                    break;
                case ON_VALUE_DECREASE:
                    if (prevValue > value) {
                        consumer.accept(this);
                    }
                    break;
                case ON_VALUE_INCREASE:
                    if (prevValue < value) {
                        consumer.accept(this);
                    }
                case ON_RECEIVE:
                    consumer.accept(this);
                    break;
            }
        }
    }

    /**
     * Adds a message listener
     *
     * @param consumer The listener to be consumed
     */
    public void addMessageListener(Consumer<ECUStat> consumer) {
        addMessageListener(consumer, UpdateMethod.ON_VALUE_CHANGE);
    }

    /**
     * Adds a message listener
     *
     * @param messageListener The listener to be consumed
     * @param updateMethod    {@link UpdateMethod}
     */
    public void addMessageListener(Consumer<ECUStat> messageListener, UpdateMethod updateMethod) {
        messageListeners.put(messageListener, updateMethod);
    }

    public void clear() {
        this.value = 0;
    }

    /**
     * Gets the current value as a signed byte
     *
     * @return The current value as a signed byte
     */
    public byte getAsByte() {
        return ByteSplit.toSignedByte(value);
    }

    /**
     * Gets the current value as a signed short
     *
     * @return The current value as a signed short
     */
    public short getAsShort() {
        return ByteSplit.toSignedShort(value);
    }

    /**
     * Gets the current value as a signed integer
     *
     * @return The current value as a signed integer
     */
    public int getAsInt() {
        return ByteSplit.toSignedInt(value);
    }

    /**
     * Gets the current value
     *
     * @return The current value
     */
    public long get() {
        return value;
    }

    public enum UpdateMethod {
        /**
         * Fire the event each time a value is received
         */
        ON_RECEIVE,

        /**
         * Fire the event each time the value changes
         */
        ON_VALUE_CHANGE,

        /**
         * Fire the event each time the value decreases
         */
        ON_VALUE_DECREASE,

        /**
         * Fire the event each time the value increases
         */
        ON_VALUE_INCREASE
    }
}
