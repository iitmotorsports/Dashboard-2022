package com.iit.dashboard2022.ecu;

import androidx.annotation.IntDef;

import com.iit.dashboard2022.util.ByteSplit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

public class ECUMsg {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ON_RECEIVE,
            ON_VALUE_CHANGE,
            ON_VALUE_DECREASE,
            ON_VALUE_INCREASE
    })
    @interface UpdateMethod {
    }

    public static final int ON_RECEIVE = 0;
    public static final int ON_VALUE_CHANGE = 1;
    public static final int ON_VALUE_DECREASE = 2;
    public static final int ON_VALUE_INCREASE = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SIGNED_BYTE,
            SIGNED_SHORT,
            SIGNED_INT,
            UNSIGNED
    })
    @interface DataType {
    }

    public static final int SIGNED_BYTE = 0;
    public static final int SIGNED_SHORT = 1;
    public static final int SIGNED_INT = 2;
    public static final int UNSIGNED = 3;

    @DataType
    private final int dataType;
    public final String stringTag, stringMsg;

    public MessageListener messageListener;
    @UpdateMethod
    private int updateMethod = ON_VALUE_CHANGE;
    public long value = 0;

    public interface MessageListener {
        void run(long val);
    }

    public ECUMsg(String stringTag, String stringMsg, @DataType int dataType) {
        this.stringTag = stringTag;
        this.stringMsg = stringMsg;
        this.dataType = dataType;
    }

    void load(HashMap<Long, ECUMsg> messageMap, ECUKeyMap keyMap) {
        messageMap.put(keyMap.requestMsgID(stringTag, stringMsg), this);
    }

    void update(long val) {
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

        if (messageListener != null)
            switch (updateMethod) {
                case ON_VALUE_CHANGE:
                    if (prevValue != value)
                        messageListener.run(val);
                    break;
                case ON_VALUE_DECREASE:
                    if (prevValue > value)
                        messageListener.run(val);
                    break;
                case ON_VALUE_INCREASE:
                    if (prevValue < value)
                        messageListener.run(val);
                case ON_RECEIVE:
                    messageListener.run(val);
                    break;
            }
    }

    public ECUMsg setUpdateMethod(@UpdateMethod int updateMethod) {
        this.updateMethod = updateMethod;
        return this;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void clear() {
        this.value = 0;
    }

}
