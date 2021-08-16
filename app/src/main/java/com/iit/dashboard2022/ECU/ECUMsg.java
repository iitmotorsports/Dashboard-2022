package com.iit.dashboard2022.ECU;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.util.ByteSplit;
import com.iit.dashboard2022.util.Toaster;

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

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            MC0Voltage,
            MC1Voltage,
            MC1Current,
            MC0Current,
            MC1BoardTemp,
            MC0BoardTemp,
            MC1MotorTemp,
            MC0MotorTemp,
            Speedometer,
            PowerGauge,
            BatteryLife,
            BMSVolt,
            BMSAmp,
            BMSHighTemp,
            BMSLowTemp,
            BMSDischargeLim,
            BMSChargeLim,
            Fault,
            Lag,
            Beat,
            StartLight
    })
    @interface MsgID {
    }

    public static final int MC0Voltage = 0;
    public static final int MC1Voltage = 1;
    public static final int MC1Current = 2;
    public static final int MC0Current = 3;
    public static final int MC1BoardTemp = 4;
    public static final int MC0BoardTemp = 5;
    public static final int MC1MotorTemp = 6;
    public static final int MC0MotorTemp = 7;
    public static final int Speedometer = 8;
    public static final int PowerGauge = 9;
    public static final int BatteryLife = 10;
    public static final int BMSVolt = 11;
    public static final int BMSAmp = 12;
    public static final int BMSHighTemp = 13;
    public static final int BMSLowTemp = 14;
    public static final int BMSDischargeLim = 15;
    public static final int BMSChargeLim = 16;
    public static final int Fault = 17;
    public static final int Lag = 18;
    public static final int Beat = 19;
    public static final int StartLight = 20;
    public static final int State = 21; // State is special, exclude from available MsgID

    private static final HashMap<Long, ECUMsg> messageMap = new HashMap<>();
    private static final HashMap<Long, STATE> stateMap = new HashMap<>();
    private static final ECUMsg[] messages = new ECUMsg[22];
    private static StateListener stateListener;
    private static ECUKeyMap keyMap;

    public enum STATE {
        Initializing("Initialize State"),
        PreCharge("PreCharge State"),
        Idle("Idle State"),
        Charging("Charging State"),
        Button("Button State"),
        Driving("Driving Mode State"),
        Fault("Fault State");

        String title;

        STATE(String title) {
            this.title = title;
        }

    }

    public interface StateListener {
        void onStateChanged(STATE state);
    }

    public static void loadMessages(@NonNull ECUKeyMap ecuKeyMap) {
        keyMap = ecuKeyMap;
        messages[MC0Voltage] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 DC BUS Voltage:", SIGNED_SHORT);
        messages[MC1Voltage] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 DC BUS Voltage:", SIGNED_SHORT);
        messages[MC1Current] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 DC BUS Current:", SIGNED_SHORT);
        messages[MC0Current] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 DC BUS Current:", SIGNED_SHORT);
        messages[MC1BoardTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 Board Temp:", SIGNED_SHORT);
        messages[MC0BoardTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 Board Temp:", SIGNED_SHORT);
        messages[MC1MotorTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 Motor Temp:", SIGNED_SHORT);
        messages[MC0MotorTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 Motor Temp:", SIGNED_SHORT);
        messages[Speedometer] = new ECUMsg("[Front Teensy]", "[ LOG ] Current Motor Speed:", SIGNED_INT);
        messages[PowerGauge] = new ECUMsg("[Front Teensy]", "[ LOG ] MC Current Power:", UNSIGNED);
        messages[BatteryLife] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS State Of Charge:", SIGNED_BYTE);
        messages[BMSVolt] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Immediate Voltage:", SIGNED_SHORT);
        messages[BMSAmp] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Pack Average Current:", SIGNED_SHORT);
        messages[BMSHighTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Pack Highest Temp:", UNSIGNED);
        messages[BMSLowTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Pack Lowest Temp:", UNSIGNED);
        messages[BMSDischargeLim] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Discharge current limit:", SIGNED_SHORT);
        messages[BMSChargeLim] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Charge current limit:", SIGNED_SHORT);
        messages[Fault] = new ECUMsg("[Front Teensy]", "[ LOG ] Fault State", UNSIGNED);
        messages[Lag] = new ECUMsg("[HeartBeat]", "[WARN]  Heartbeat is taking too long", UNSIGNED);
        messages[Beat] = new ECUMsg("[HeartBeat]", "[ LOG ] Beat", UNSIGNED);
        messages[StartLight] = new ECUMsg("[Front Teensy]", "[ LOG ] Start Light", UNSIGNED);
        messages[State] = new ECUMsg("[Front Teensy]", "[ LOG ] Current State", UNSIGNED);
        messages[State].setMessageListener(val -> {
            if (stateListener != null)
                stateListener.onStateChanged(stateMap.get(val));
        });
    }

    public static void setGlobalStateListener(StateListener globalStateListener) {
        stateListener = globalStateListener;
    }

    public static void loadMessageKeys() {
        messageMap.clear();
        for (ECUMsg msg : messages) {
            msg.load();
        }
        stateMap.clear();
        for (STATE state : STATE.values()) {
            Integer tagID = keyMap.getTagID(state.title);
            if (tagID == null) {
                Toaster.showToast("Failed to set State Enum for " + state.title, Toaster.WARNING);
            } else {
                stateMap.put(Long.valueOf(tagID), state);
            }
        }
    }

    @Nullable
    public static ECUMsg updateMessages(long msgKey, long value) {
        ECUMsg msg = messageMap.get(msgKey);
        if (msg != null)
            msg.update(value);
        return msg;
    }

    @Nullable
    public static ECUMsg getMessage(long msgKey) {
        return messageMap.get(msgKey);
    }

    public static ECUMsg getMessage(@MsgID int msgID) {
        return messages[msgID];
    }

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

    private void load() {
        messageMap.put(keyMap.requestMsgID(stringTag, stringMsg), this);
    }

    private void update(long val) {
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
