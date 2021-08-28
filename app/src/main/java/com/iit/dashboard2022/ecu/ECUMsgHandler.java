package com.iit.dashboard2022.ecu;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.util.Toaster;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

public class ECUMsgHandler {

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

    private final HashMap<Long, String> faultMap = new HashMap<>(ECUFaults.FAULTS.length);
    private final HashMap<Long, ECUMsg> messageMap = new HashMap<>();
    private final HashMap<Long, STATE> stateMap = new HashMap<>();
    private final ECUMsg[] messages = new ECUMsg[22];
    private final ECUKeyMap keyMap;

    private StateListener stateListener;

    public enum STATE { // Use actual name, brackets are added on when matching to actual state name
        Initializing("Teensy Initialize"),
        PreCharge("PreCharge State"),
        Idle("Idle State"),
        Charging("Charging State"),
        Button("Button State"),
        Driving("Driving Mode State"),
        Fault("Fault State");

        final String title;

        STATE(String title) {
            this.title = title;
        }

    }

    public interface StateListener {
        void onStateChanged(STATE state);
    }

    public ECUMsgHandler(@NonNull ECUKeyMap ecuKeyMap) {
        keyMap = ecuKeyMap;
        messages[MC0Voltage] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 DC BUS Voltage:", ECUMsg.SIGNED_SHORT);
        messages[MC1Voltage] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 DC BUS Voltage:", ECUMsg.SIGNED_SHORT);
        messages[MC1Current] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 DC BUS Current:", ECUMsg.SIGNED_SHORT);
        messages[MC0Current] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 DC BUS Current:", ECUMsg.SIGNED_SHORT);
        messages[MC1BoardTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 Board Temp:", ECUMsg.SIGNED_SHORT);
        messages[MC0BoardTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 Board Temp:", ECUMsg.SIGNED_SHORT);
        messages[MC1MotorTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC1 Motor Temp:", ECUMsg.SIGNED_SHORT);
        messages[MC0MotorTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] MC0 Motor Temp:", ECUMsg.SIGNED_SHORT);
        messages[Speedometer] = new ECUMsg("[Front Teensy]", "[ LOG ] Current Motor Speed:", ECUMsg.SIGNED_INT);
        messages[PowerGauge] = new ECUMsg("[Front Teensy]", "[ LOG ] MC Current Power:", ECUMsg.UNSIGNED);
        messages[BatteryLife] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS State Of Charge:", ECUMsg.SIGNED_BYTE);
        messages[BMSVolt] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Immediate Voltage:", ECUMsg.SIGNED_SHORT);
        messages[BMSAmp] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Pack Average Current:", ECUMsg.SIGNED_SHORT);
        messages[BMSHighTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Pack Highest Temp:", ECUMsg.UNSIGNED);
        messages[BMSLowTemp] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Pack Lowest Temp:", ECUMsg.UNSIGNED);
        messages[BMSDischargeLim] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Discharge current limit:", ECUMsg.SIGNED_SHORT);
        messages[BMSChargeLim] = new ECUMsg("[Front Teensy]", "[ LOG ] BMS Charge current limit:", ECUMsg.SIGNED_SHORT);
        messages[Fault] = new ECUMsg("[Front Teensy]", "[ LOG ] Fault State", ECUMsg.UNSIGNED);
        messages[Lag] = new ECUMsg("[HeartBeat]", "[WARN]  Heartbeat is taking too long", ECUMsg.UNSIGNED);
        messages[Beat] = new ECUMsg("[HeartBeat]", "[ LOG ] Beat", ECUMsg.UNSIGNED);
        messages[StartLight] = new ECUMsg("[Front Teensy]", "[ LOG ] Start Light", ECUMsg.UNSIGNED);
        messages[State] = new ECUMsg("[Front Teensy]", "[ LOG ] Current State", ECUMsg.UNSIGNED);
        messages[State].addMessageListener(val -> {
            if (stateListener != null)
                stateListener.onStateChanged(stateMap.get(val));
        });
    }

    private void getFaultStrIDs() {
        faultMap.clear();
        for (String fMsg : ECUFaults.FAULTS) {
            Integer i = keyMap.getStrID(fMsg);
            if (i != null)
                faultMap.put(i.longValue(), fMsg);
        }
    }

    public ECUMsg[] getMessageArray() {
        return messages;
    }

    public void setGlobalStateListener(StateListener globalStateListener) {
        stateListener = globalStateListener;
    }

    public void loadMessageKeys() {
        messageMap.clear();
        for (ECUMsg msg : messages) {
            msg.load(messageMap, keyMap);
        }
        stateMap.clear();
        for (STATE state : STATE.values()) {
            Integer tagID = keyMap.getTagID(String.format("[%s]", state.title));
            if (tagID == null) {
                Toaster.showToast("Failed to set State Enum for " + state.title, Toaster.WARNING);
            } else {
                stateMap.put(Long.valueOf(tagID), state);
            }
        }
        getFaultStrIDs();
    }

    @Nullable
    public String checkFaults(long strKey) {
        return faultMap.get(strKey);
    }

    @Nullable
    public ECUMsg updateMessages(long msgKey, long value) {
        ECUMsg msg = messageMap.get(msgKey);
        if (msg != null)
            msg.update(value);
        return msg;
    }

    public long requestValue(@MsgID int msgID) {
        return messages[msgID].value;
    }

    @Nullable
    public ECUMsg getMessage(long msgKey) {
        return messageMap.get(msgKey);
    }

    public ECUMsg getMessage(@MsgID int msgID) {
        return messages[msgID];
    }
}
