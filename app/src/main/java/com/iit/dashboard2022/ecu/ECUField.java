package com.iit.dashboard2022.ecu;

import com.google.common.collect.Maps;
import com.iit.dashboard2022.util.ByteSplit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ECUField {

    private final String identifier;
    private final int tag;
    private final DataType dataType;
    public final Map<Consumer<Long>, UpdateMethod> messageListeners = Maps.newHashMap();
    public long value = 0;

    public ECUField(String identifier, int tag, DataType dataType) {
        this.identifier = identifier;
        this.tag = tag;
        this.dataType = dataType;
    }

    /**
     * Update the message from the ECU
     *
     * @param val The value of the field
     */
    public void update(long val) {
        long prevValue = this.value;
        switch (this.dataType) {
            case SIGNED_BYTE:
                this.value = ByteSplit.toSignedByte(val);
                break;
            case SIGNED_SHORT:
                this.value = ByteSplit.toSignedShort(val);
                break;
            case SIGNED_INT:
                this.value = ByteSplit.toSignedInt(val);
                break;
            case UNSIGNED:
                this.value = val;
        }

        for (Map.Entry<Consumer<Long>, UpdateMethod> entry : messageListeners.entrySet()) {
            Consumer<Long> consumer = entry.getKey();
            switch (entry.getValue()) {
                case ON_VALUE_CHANGE:
                    if (prevValue != value) {
                        consumer.accept(val);
                    }
                    break;
                case ON_VALUE_DECREASE:
                    if (prevValue > value) {
                        consumer.accept(val);
                    }
                    break;
                case ON_VALUE_INCREASE:
                    if (prevValue < value) {
                        consumer.accept(val);
                    }
                case ON_RECEIVE:
                    consumer.accept(val);
                    break;
            }
        }
    }

    /**
     * Adds a message listener
     *
     * @param consumer The listener to be consumed
     */
    public void addMessageListener(Consumer<Long> consumer) {
        addMessageListener(consumer, UpdateMethod.ON_VALUE_CHANGE);
    }

    /**
     * Adds a message listener
     *
     * @param messageListener The listener to be consumed
     * @param updateMethod    {@link UpdateMethod}
     */
    public void addMessageListener(Consumer<Long> messageListener, UpdateMethod updateMethod) {
        messageListeners.put(messageListener, updateMethod);
    }

    public void clear() {
        this.value = 0;
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

    public enum DataType {
        SIGNED_BYTE,
        SIGNED_SHORT,
        SIGNED_INT,
        UNSIGNED
    }
}
