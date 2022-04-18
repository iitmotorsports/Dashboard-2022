package com.iit.dashboard2022.ecu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A representation of an incoming ECU payload
 *
 * @author Noah Husby
 */
public class ECUPayload {

    private final long epoch;
    private final long callerId;
    private final long stringId;
    private final long value;
    private final long msgId;
    private final byte[] rawData;

    public ECUPayload(byte[] data) {
        this(System.currentTimeMillis(), data);
    }

    public ECUPayload(long epoch, byte[] data) {
        this.epoch = epoch;
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        callerId = buf.getShort() & 0xffff;
        stringId = buf.getShort() & 0xffff;
        value = buf.getInt() & 0xffffffffL;
        msgId = buf.getInt(0);
        this.rawData = data;
    }

    /**
     * Gets the epoch of the payload.
     *
     * @return Epoch in milliseconds.
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * Gets the caller id of the payload.
     *
     * @return Caller ID of payload.
     */
    public long getCallerId() {
        return callerId;
    }

    /**
     * Gets the string id of the payload.
     *
     * @return String id of the payload.
     */
    public long getStringId() {
        return stringId;
    }

    /**
     * Gets the value of the payload.
     *
     * @return The value of the payload.
     */
    public long getValue() {
        return value;
    }

    /**
     * Gets the message id of the payload.
     *
     * @return The message id of the payload.
     */
    public long getMsgId() {
        return msgId;
    }

    /**
     * Gets the raw data of the payload.
     *
     * @return The raw data of the payloa.
     */
    public byte[] getRawData() {
        return rawData;
    }
}
