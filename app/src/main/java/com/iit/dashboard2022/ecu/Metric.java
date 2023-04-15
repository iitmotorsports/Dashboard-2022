package com.iit.dashboard2022.ecu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public enum Metric {

    BRAKE(1),
    ACC_1(2),
    ACC_2(3),
    MC0_VOLTAGE(4),
    MC1_VOLTAGE(5),
    MC1_CURRENT(6),
    MC0_CURRENT(7),
    MC1_BOARD_TEMP(8),
    MC0_BOARD_TEMP(9),
    MC1_MOTOR_TEMP(10),
    MC0_MOTOR_TEMP(11),
    SPEEDOMETER(12),
    POWER_GUAGE(13),
    SOC(14),
    STACK_VOLTAGE(15),
    STACK_CURRENT(16),
    STACK_HIGH_TEMP(17),
    STACK_LOW_TEMP(18),
    BMS_DISCHARGE_LIM(19),
    BMS_CHARGE_LIM(20),
    FAULT(21),
    LAG(22),
    BEAT(23),
    START_LIGHT(24),
    STATE(25),
    SERIAL_VAR_RESPONSE(26),
    STEER(27);

    private final int id;
    private int value = 0;
    public final Map<Consumer<Metric>, UpdateMethod> messageListeners = new ConcurrentHashMap<>();

    Metric(int id) {
        this.id = id;
    }

    /**
     * Gets the id of the metric.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the metric.
     *
     * @return The name of the metric
     */
    public String getName() {
        return this.name();
    }

    /**
     * Update the message from the ECU
     *
     * @param val The value of the field
     * @implNote This should only be called internal to {@link ECU}
     */
    public void update(int val) {
        long prevValue = this.value;
        this.value = val;

        for (Map.Entry<Consumer<Metric>, UpdateMethod> entry : messageListeners.entrySet()) {
            Consumer<Metric> consumer = entry.getKey();
            switch (entry.getValue()) {
                case ON_VALUE_CHANGE:
                    if (prevValue != value) {
                        consumer.accept(this);
                    }
                    break;
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
    public void addMessageListener(Consumer<Metric> consumer) {
        addMessageListener(consumer, UpdateMethod.ON_VALUE_CHANGE);
    }

    /**
     * Adds a message listener
     *
     * @param messageListener The listener to be consumed
     * @param updateMethod    {@link UpdateMethod}
     */
    public void addMessageListener(Consumer<Metric> messageListener, UpdateMethod updateMethod) {
        messageListeners.put(messageListener, updateMethod);
    }

    public void clear() {
        this.value = 0;
    }

    /**
     * Gets the current value
     *
     * @return The current value
     */
    public int getValue() {
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
    }

    public static Metric getMetricById(int id) {
        for (Metric metric : values()) {
            if (metric.getId() == id) {
                return metric;
            }
        }
        return null;
    }
}

